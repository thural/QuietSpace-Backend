from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.repositories.post import PostRepository
from app.schemas.post import PostCreate, PostUpdate, PostResponse
from app.services.post_service import PostService

router = APIRouter()


@router.post("/", response_model=PostResponse, status_code=status.HTTP_201_CREATED)
async def create_post(post_in: PostCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PostService(db)
    post = await service.create_post(current_user.id, post_in)
    return post


@router.get("/", response_model=list[PostResponse])
async def get_posts(
    user_id: UUID | None = Query(None),
    q: str | None = Query(None),
    page: int = Query(1, ge=1),
    size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = PostService(db)
    offset = (page - 1) * size
    if user_id:
        posts = await service.get_posts(user_id, limit=size, offset=offset)
    elif q:
        repo = PostRepository(db)
        posts = await repo.search(q, limit=size)
    else:
        repo = PostRepository(db)
        posts = await repo.get_all()
    return posts


@router.get("/{post_id}", response_model=PostResponse)
async def get_post(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = PostService(db)
    post = await service.get_post(post_id)
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    return post


@router.put("/{post_id}", response_model=PostResponse)
async def update_post(post_id: UUID, post_in: PostUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PostService(db)
    post = await service.get_post(post_id)
    if not post or post.author_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    updated = await service.update_post(post_id, post_in)
    return updated


@router.delete("/{post_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_post(post_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PostService(db)
    post = await service.get_post(post_id)
    if not post or post.author_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    await service.delete_post(post_id)
