from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.reaction_service import ReactionService
from app.schemas.reaction import ReactionCreate, ReactionResponse

router = APIRouter()


@router.post("/", response_model=ReactionResponse, status_code=status.HTTP_201_CREATED)
async def toggle_reaction(reaction_in: ReactionCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    reaction = await service.toggle_reaction(current_user.id, reaction_in)
    return reaction


@router.get("/post/{post_id}", response_model=list[ReactionResponse])
async def get_post_reactions(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = ReactionService(db)
    reactions = await service.get_reactions(post_id=post_id)
    return reactions
