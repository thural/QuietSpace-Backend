from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user, get_cache
from app.core.cache import CacheService
from app.models.user import User
from app.repositories.user import UserRepository
from app.repositories.profile_settings import ProfileSettingsRepository
from app.schemas.user import UserUpdate, UserResponse
from app.schemas.profile_settings import ProfileSettingsUpdate, ProfileSettingsResponse
from app.services.user_service import UserService

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


@router.put("/me", response_model=UserResponse)
async def update_me(user_in: UserUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    update_data = user_in.model_dump(exclude_unset=True)
    for field, value in update_data.items():
        setattr(current_user, field, value)
    await repo.update(current_user)
    return current_user


@router.get("/search", response_model=list[UserResponse])
async def search_users(q: str = Query(..., min_length=1), db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    users = await repo.search(q, limit=20)
    return users


@router.get("/{user_id}", response_model=UserResponse)
async def get_user(user_id: UUID, db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = UserService(db, cache_service=cache)
    user = await service.get_user(str(user_id))
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user


@router.post("/{user_id}/follow")
async def follow_user(
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
async def unfollow_user(
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
async def get_followers(user_id: UUID, db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = UserService(db, cache_service=cache)
    return await service.get_followers(user_id)


@router.get("/{user_id}/following")
async def get_following(user_id: UUID, db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = UserService(db, cache_service=cache)
    return await service.get_following(user_id)


@router.get("/{user_id}/save", response_model=UserResponse)
async def get_user_with_relations(user_id: UUID, db: AsyncSession = Depends(get_db)):
    repo = UserRepository(db)
    user = await repo.get_with_posts(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user
