from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.reaction import Reaction
from app.repositories.reaction import ReactionRepository
from app.schemas.reaction import ReactionCreate
from app.enums.reaction_type import ReactionType


class ReactionService:
    def __init__(self, session: AsyncSession):
        self.reaction_repo = ReactionRepository(session)

    async def create_reaction(self, user_id: UUID, reaction_in: ReactionCreate) -> Reaction:
        existing = await self.reaction_repo.get_by_user_and_target(
            user_id, reaction_in.post_id, reaction_in.comment_id
        )
        if existing:
            raise ValueError("Reaction already exists")
        reaction = Reaction(**reaction_in.model_dump(), user_id=user_id)
        return await self.reaction_repo.create(reaction)

    async def delete_reaction(self, reaction_id: UUID) -> bool:
        return await self.reaction_repo.delete(reaction_id)

    async def get_reactions(self, post_id: UUID | None = None, comment_id: UUID | None = None, cursor: str | None = None, limit: int = 20) -> tuple[list[Reaction], str | None, bool]:
        if post_id:
            return await self.reaction_repo.get_by_post(post_id, cursor, limit)
        if comment_id:
            return await self.reaction_repo.get_by_comment(comment_id, cursor, limit)
        return [], None, False

    async def get_user_reactions(self, user_id: UUID, reaction_type: ReactionType | None = None, cursor: str | None = None, limit: int = 20) -> tuple[list[Reaction], str | None, bool]:
        return await self.reaction_repo.get_by_user(user_id, reaction_type, cursor, limit)

    async def get_reaction_count(self, post_id: UUID) -> int:
        return await self.reaction_repo.count_by_post(post_id)
