from fastapi import APIRouter, Depends, HTTPException, status, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user, get_optional_current_user, get_cache
from app.api.websocket.manager import manager
from app.core.cache import CacheService
from app.core.rate_limiter import limiter, SENSITIVE_LIMIT
from app.models.user import User
from app.enums.role import Role
from app.repositories.user import UserRepository
from app.repositories.profile_settings import ProfileSettingsRepository
from app.schemas.user import UserUpdate, UserResponse
from app.schemas.profile_settings import ProfileSettingsUpdate, ProfileSettingsResponse
from app.services.user_service import UserService
from app.services.block_service import BlockService

router = APIRouter()


@router.get("/me", response_model=UserResponse)
async def get_me(current_user: User = Depends(get_current_user)):
    return current_user


@router.get("/me/settings", response_model=ProfileSettingsResponse)
async def get_my_settings(current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    repo = ProfileSettingsRepository(db)
    settings = await repo.get_by_user_id(current_user.id)
    if not settings:
        raise HTTPException(status_code=404, detail="Settings not found")
    return settings


@router.put("/me/settings", response_model=ProfileSettingsResponse)
async def update_my_settings(
    settings_in: ProfileSettingsUpdate,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    repo = ProfileSettingsRepository(db)
    settings = await repo.get_by_user_id(current_user.id)
    if not settings:
        from app.models.profile_settings import ProfileSettings
        settings = ProfileSettings(user_id=current_user.id)
        db.add(settings)
    update_data = settings_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(settings, field, value)
    await repo.update(settings)
    return settings


@router.patch("/me", response_model=UserResponse)
@limiter.limit(SENSITIVE_LIMIT)
async def update_me(request: Request, user_in: UserUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    update_data = user_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(current_user, field, value)
    await repo.update(current_user)
    return current_user


@router.post("/me/block/{user_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def block_user_me(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = BlockService(db, cache_service=cache)
    success = await service.block_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot block user")
    return {"message": "User blocked successfully"}


@router.delete("/me/block/{user_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def unblock_user_me(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = BlockService(db, cache_service=cache)
    success = await service.unblock_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot unblock user")
    return {"message": "User unblocked successfully"}


@router.get("/me/blocked")
async def get_blocked_users_me(
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    from app.schemas.pagination import OffsetResponse
    service = BlockService(db, cache_service=cache)
    users, total = await service.get_blocked_users(current_user.id, page=page, size=size)
    return OffsetResponse(
        items=users,
        total=total,
        page=page,
        size=size,
        pages=(total + size - 1) // size if total > 0 else 0,
    )


@router.get("/query")
async def query_users(
    username: str | None = Query(None, min_length=1),
    firstname: str | None = Query(None, min_length=1),
    lastname: str | None = Query(None, min_length=1),
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    from app.schemas.pagination import OffsetResponse
    service = UserService(db, cache_service=cache)
    user_id = current_user.id if current_user else None
    users, total = await service.advanced_search(
        username=username,
        firstname=firstname,
        lastname=lastname,
        page=page,
        size=size,
        current_user_id=user_id,
    )
    return OffsetResponse(
        items=users,
        total=total,
        page=page,
        size=size,
        pages=(total + size - 1) // size if total > 0 else 0,
    )


@router.get("/search")
async def search_users(
    q: str = Query(..., min_length=1),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    from app.schemas.pagination import CursorResponse
    from app.schemas.user import UserResponse
    service = UserService(db)
    user_id = current_user.id if current_user else None
    users, next_cursor, has_more = await service.search_users(q, current_user_id=user_id, cursor=cursor, limit=limit)
    return CursorResponse(items=users, next_cursor=next_cursor, has_more=has_more)


@router.get("/online", response_model=list[UserResponse])
async def get_online_users(db: AsyncSession = Depends(get_db)):
    online_user_ids = await manager.get_online_users()
    if not online_user_ids:
        return []
    service = UserService(db)
    users = await service.get_online_users_details(online_user_ids)
    return users


@router.get("/{user_id}", response_model=UserResponse)
async def get_user(user_id: UUID, db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = UserService(db, cache_service=cache)
    user = await service.get_user(str(user_id))
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.delete("/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
@limiter.limit(SENSITIVE_LIMIT)
async def delete_user(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = UserService(db, cache_service=cache)
    success = await service.delete_user(user_id, current_user.id, is_admin=current_user.role == Role.ADMIN)
    if not success:
        raise HTTPException(status_code=404, detail="User not found")


@router.post("/{user_id}/follow")
@limiter.limit(SENSITIVE_LIMIT)
async def follow_user(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = UserService(db, cache_service=cache)
    success = await service.follow_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot follow user")
    return {"message": "User followed successfully"}


@router.delete("/{user_id}/follow")
@limiter.limit(SENSITIVE_LIMIT)
async def unfollow_user(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = UserService(db, cache_service=cache)
    success = await service.unfollow_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot unfollow user")
    return {"message": "User unfollowed successfully"}


@router.get("/{user_id}/followers")
async def get_followers(
    user_id: UUID,
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    from app.schemas.pagination import OffsetResponse
    service = UserService(db, cache_service=cache)
    users, total = await service.get_followers(user_id, page=page, size=size)
    return OffsetResponse(
        items=users,
        total=total,
        page=page,
        size=size,
        pages=(total + size - 1) // size if total > 0 else 0,
    )


@router.get("/{user_id}/following")
async def get_following(
    user_id: UUID,
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    from app.schemas.pagination import OffsetResponse
    service = UserService(db, cache_service=cache)
    users, total = await service.get_following(user_id, page=page, size=size)
    return OffsetResponse(
        items=users,
        total=total,
        page=page,
        size=size,
        pages=(total + size - 1) // size if total > 0 else 0,
    )


@router.delete("/{user_id}/followers/{follower_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def remove_follower_rest(
    request: Request,
    user_id: UUID,
    follower_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    if current_user.id != user_id:
        raise HTTPException(status_code=403, detail="Not authorized")
    service = UserService(db, cache_service=cache)
    success = await service.remove_follower(current_user.id, follower_id)
    if not success:
        raise HTTPException(status_code=400, detail="Not a follower")
    return {"message": "Follower removed successfully"}


@router.post("/followers/remove/{follower_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def remove_follower_deprecated(
    request: Request,
    follower_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = UserService(db, cache_service=cache)
    success = await service.remove_follower(current_user.id, follower_id)
    if not success:
        raise HTTPException(status_code=400, detail="Not a follower")
    return {"message": "Follower removed successfully"}


@router.get("/{user_id}/detail", response_model=UserResponse)
async def get_user_detail(user_id: UUID, db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    user = await repo.get_with_posts(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.get("/{user_id}/save", response_model=UserResponse)
async def get_user_with_relations(user_id: UUID, db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    user = await repo.get_with_posts(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.post("/profile/block/{user_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def block_user(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = BlockService(db, cache_service=cache)
    success = await service.block_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot block user")
    return {"message": "User blocked successfully"}


@router.delete("/profile/block/{user_id}")
@limiter.limit(SENSITIVE_LIMIT)
async def unblock_user(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    service = BlockService(db, cache_service=cache)
    success = await service.unblock_user(current_user.id, user_id)
    if not success:
        raise HTTPException(status_code=400, detail="Cannot unblock user")
    return {"message": "User unblocked successfully"}


@router.get("/profile/blocked")
async def get_blocked_users(
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cache: CacheService = Depends(get_cache),
):
    from app.schemas.pagination import OffsetResponse
    service = BlockService(db, cache_service=cache)
    users, total = await service.get_blocked_users(current_user.id, page=page, size=size)
    return OffsetResponse(
        items=users,
        total=total,
        page=page,
        size=size,
        pages=(total + size - 1) // size if total > 0 else 0,
    )
