from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.comment import Comment
from app.repositories.base import BaseRepository


class CommentRepository(BaseRepository[Comment]):
    def __init__(self, session: AsyncSession):
        super().__init__(Comment, session)

    async def get_by_post(self, post_id: UUID) -> list[Comment]:
        result = await self.session.execute(
            select(Comment)
            .where(Comment.post_id == post_id, Comment.parent_id == None)
            .order_by(Comment.created_at.asc())
        )
        return result.scalars().all()

    async def get_replies(self, parent_id: UUID) -> list[Comment]:
        result = await self.session.execute(
            select(Comment)
            .where(Comment.parent_id == parent_id)
            .order_by(Comment.created_at.asc())
        )
        return result.scalars().all()


comment_repository = CommentRepository
