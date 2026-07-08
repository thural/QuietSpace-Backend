from fastapi import APIRouter, Depends, HTTPException, Query, status, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.services.reaction_service import ReactionService
from app.schemas.reaction import ReactionCreate, ReactionResponse
from app.schemas.pagination import CursorResponse
from app.enums.reaction_type import ReactionType

router = APIRouter()


@router.get("/user", response_model=CursorResponse[ReactionResponse])
async def get_user_reactions(
    type: ReactionType | None = Query(None),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = ReactionService(db)
    reactions, next_cursor, has_more = await service.get_user_reactions(
        current_user.id, reaction_type=type, cursor=cursor, limit=limit
    )
    return CursorResponse(items=reactions, next_cursor=next_cursor, has_more=has_more)


@router.get("/content", response_model=list[ReactionResponse])
async def get_content_reactions(
    content_type: str = Query(..., pattern="^(post|comment)$"),
    content_id: UUID = Query(...),
    db: AsyncSession = Depends(get_db),
):
    service = ReactionService(db)
    kwargs = {f"{content_type}_id": content_id}
    reactions = await service.get_reactions(**kwargs)
    return reactions


@router.post("", response_model=ReactionResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_reaction(request: Request, reaction_in: ReactionCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    try:
        reaction = await service.create_reaction(current_user.id, reaction_in)
    except ValueError:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Reaction already exists")
    return reaction


@router.delete("/{reaction_id}", status_code=status.HTTP_204_NO_CONTENT)
@limiter.limit(CONTENT_LIMIT)
async def delete_reaction(request: Request, reaction_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    success = await service.delete_reaction(reaction_id)
    if not success:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Reaction not found")


@router.get("/post/{post_id}", response_model=list[ReactionResponse])
async def get_post_reactions(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    reactions = await service.get_reactions(post_id=post_id)
    return reactions


@router.get("/count/{post_id}")
async def get_reaction_count(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    count = await service.get_reaction_count(post_id)
    return {"post_id": str(post_id), "count": count}
