from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.reaction import Reaction
from app.repositories.reaction import ReactionRepository
from app.schemas.reaction import ReactionCreate


class ReactionService:
    def __init__(self, session: AsyncSession):
        self.reaction_repo = ReactionRepository(session)

    async def toggle_reaction(self, user_id: UUID, reaction_in: ReactionCreate) -> Reaction:
        existing = await self.reaction_repo.get_by_user_and_target(
            user_id, reaction_in.post_id, reaction_in.comment_id
        )
        if existing:
            await self.reaction_repo.delete(existing.id)
            return existing
        reaction = Reaction(**reaction_in.model_dump(), user_id=user_id)
        return await self.reaction_repo.create(reaction)

    async def get_reactions(self, post_id: UUID | None = None, comment_id: UUID | None = None) -> list[Reaction]:
        if post_id:
            result = await self.reaction_repo.session.execute(
                select(Reaction).where(Reaction.post_id == post_id)
            )
            return result.scalars().all()
        if comment_id:
            result = await self.reaction_repo.session.execute(
                select(Reaction).where(Reaction.comment_id == comment_id)
            )
            return result.scalars().all()
        return []
