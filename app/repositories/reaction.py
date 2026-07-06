from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.reaction import Reaction
from app.repositories.base import BaseRepository


class ReactionRepository(BaseRepository[Reaction]):
    def __init__(self, session: AsyncSession):
        super().__init__(Reaction, session)

    async def get_by_user_and_target(self, user_id: UUID, post_id: UUID | None = None, comment_id: UUID | None = None) -> Reaction | None:
        stmt = select(Reaction).where(Reaction.user_id == user_id)
        if post_id:
            stmt = stmt.where(Reaction.post_id == post_id, Reaction.comment_id == None)
        if comment_id:
            stmt = stmt.where(Reaction.comment_id == comment_id, Reaction.post_id == None)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def count_by_post(self, post_id: UUID) -> int:
        result = await self.session.execute(
            select(Reaction).where(Reaction.post_id == post_id)
        )
        return len(result.scalars().all())


reaction_repository = ReactionRepository
