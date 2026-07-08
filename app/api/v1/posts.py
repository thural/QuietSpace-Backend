from fastapi import APIRouter, Depends, HTTPException, status, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user, get_optional_current_user, get_cache
from app.core.cache import CacheService
from app.models.user import User
from app.repositories.post import PostRepository
from app.schemas.pagination import CursorResponse
from app.schemas.post import PostCreate, PostUpdate, PostResponse, RepostRequest
from app.schemas.poll import VoteRequest
from app.services.post_service import PostService
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.services.poll_service import PollService

router = APIRouter()


@router.post("", response_model=PostResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_post(request: Request, post_in: PostCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = PostService(db, cache_service=cache)
    post = await service.create_post(current_user.id, post_in)
    return post


@router.post("/repost", response_model=PostResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_repost(request: Request, repost_in: RepostRequest, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = PostService(db, cache_service=cache)
    try:
        post = await service.create_repost(current_user.id, repost_in)
    except ValueError as e:
        raise HTTPException(status_code=403, detail=str(e))
    return post


@router.get("", response_model=CursorResponse[PostResponse])
async def get_posts(
    user_id: UUID | None = Query(None),
    q: str | None = Query(None),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = PostService(db)
    if q:
        repo = PostRepository(db)
        posts = await repo.search(q, limit=limit)
        return CursorResponse(items=posts, next_cursor=None, has_more=False)
    posts, next_cursor, has_more = await service.get_posts(
        author_id=user_id, cursor=cursor, limit=limit
    )
    return CursorResponse(items=posts, next_cursor=next_cursor, has_more=has_more)


@router.get("/{post_id}", response_model=PostResponse)
async def get_post(post_id: UUID, current_user: User | None = Depends(get_optional_current_user), db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = PostService(db, cache_service=cache)
    user_id = current_user.id if current_user else None
    post = await service.get_post(post_id, current_user_id=user_id)
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    return post


@router.patch("/{post_id}", response_model=PostResponse)
@limiter.limit(CONTENT_LIMIT)
async def update_post(request: Request, post_id: UUID, post_in: PostUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = PostService(db, cache_service=cache)
    post = await service.get_post(post_id)
    if not post or post.author_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    updated = await service.update_post(post_id, post_in)
    return updated


@router.post("/{post_id}/save")
@limiter.limit(CONTENT_LIMIT)
async def save_post(request: Request, post_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    from app.models.saved_post import SavedPost
    from sqlalchemy import select
    result = await db.execute(
        select(SavedPost).where(
            SavedPost.user_id == current_user.id,
            SavedPost.post_id == post_id,
        )
    )
    existing = result.scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=400, detail="Post already saved")
    saved = SavedPost(user_id=current_user.id, post_id=post_id)
    db.add(saved)
    await db.commit()
    return {"message": "Post saved"}


@router.delete("/{post_id}/save", status_code=status.HTTP_204_NO_CONTENT)
async def unsave_post(post_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    from app.models.saved_post import SavedPost
    from sqlalchemy import select
    result = await db.execute(
        select(SavedPost).where(
            SavedPost.user_id == current_user.id,
            SavedPost.post_id == post_id,
        )
    )
    saved = result.scalar_one_or_none()
    if not saved:
        raise HTTPException(status_code=404, detail="Saved post not found")
    await db.delete(saved)
    await db.commit()


@router.get("/saved", response_model=CursorResponse[PostResponse])
async def get_saved_posts(
    current_user: User = Depends(get_current_user),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    repo = PostRepository(db)
    posts, next_cursor, has_more = await repo.get_saved_paginated(current_user.id, cursor, limit)
    return CursorResponse(items=posts, next_cursor=next_cursor, has_more=has_more)


@router.get("/user/{user_id}", response_model=CursorResponse[PostResponse])
async def get_posts_by_user(
    user_id: UUID,
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = PostService(db)
    posts, next_cursor, has_more = await service.get_posts(
        author_id=user_id, cursor=cursor, limit=limit
    )
    return CursorResponse(items=posts, next_cursor=next_cursor, has_more=has_more)


@router.get("/commented/{user_id}", response_model=CursorResponse[PostResponse])
async def get_commented_posts(
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = PostService(db)
    posts, next_cursor, has_more = await service.get_commented_posts(user_id, cursor=cursor, limit=limit, current_user_id=current_user.id)
    return CursorResponse(items=posts, next_cursor=next_cursor, has_more=has_more)


@router.delete("/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_post(post_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db), cache: CacheService = Depends(get_cache)):
    service = PostService(db, cache_service=cache)
    post = await service.get_post(post_id)
    if not post or post.author_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    await service.delete_post(post_id)


@router.post("/vote-poll", status_code=status.HTTP_200_OK)
@limiter.limit(CONTENT_LIMIT)
async def vote_poll(
    request: Request,
    vote_in: VoteRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = PollService(db)
    success = await service.vote(current_user.id, vote_in)
    return {"message": "Vote recorded successfully"}
