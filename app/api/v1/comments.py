from fastapi import APIRouter, Depends, HTTPException, Query, status, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user, get_optional_current_user
from app.models.user import User
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.services.comment_service import CommentService
from app.schemas.comment import CommentCreate, CommentUpdate, CommentResponse
from app.schemas.pagination import CursorResponse

router = APIRouter()


@router.post("", response_model=CommentResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_comment(request: Request, comment_in: CommentCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = CommentService(db)
    comment = await service.create_comment(current_user.id, comment_in)
    return comment


@router.get("/post/{post_id}", response_model=CursorResponse[CommentResponse])
async def get_comments(
    post_id: UUID,
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = CommentService(db)
    user_id = current_user.id if current_user else None
    comments, next_cursor, has_more = await service.get_comments(post_id, cursor, limit, current_user_id=user_id)
    return CursorResponse(items=comments, next_cursor=next_cursor, has_more=has_more)


@router.get("/{comment_id}/replies", response_model=CursorResponse[CommentResponse])
async def get_replies(
    comment_id: UUID,
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = CommentService(db)
    user_id = current_user.id if current_user else None
    replies, next_cursor, has_more = await service.get_replies(comment_id, cursor, limit, current_user_id=user_id)
    return CursorResponse(items=replies, next_cursor=next_cursor, has_more=has_more)


@router.put("/{comment_id}", response_model=CommentResponse)
@limiter.limit(CONTENT_LIMIT)
async def update_comment(request: Request, comment_id: UUID, comment_in: CommentUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = CommentService(db)
    comment = await service.update_comment(comment_id, comment_in)
    if not comment:
        raise HTTPException(status_code=404, detail="Comment not found")
    return comment


@router.delete("/{comment_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_comment(comment_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = CommentService(db)
    success = await service.delete_comment(comment_id)
    if not success:
        raise HTTPException(status_code=404, detail="Comment not found")
