from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.comment import Comment
from app.repositories.comment import CommentRepository
from app.schemas.comment import CommentCreate, CommentUpdate


class CommentService:
    def __init__(self, session: AsyncSession):
        self.comment_repo = CommentRepository(session)

    async def create_comment(self, author_id: UUID, comment_in: CommentCreate) -> Comment:
        comment = Comment(**comment_in.model_dump(), author_id=author_id)
        return await self.comment_repo.create(comment)

    async def update_comment(self, comment_id: UUID, comment_in: CommentUpdate) -> Comment | None:
        comment = await self.comment_repo.get(comment_id)
        if not comment:
            return None
        update_data = comment_in.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(comment, field, value)
        return await self.comment_repo.update(comment)

    async def delete_comment(self, comment_id: UUID) -> bool:
        return await self.comment_repo.delete(comment_id)

    async def get_comments(self, post_id: UUID) -> list[Comment]:
        return await self.comment_repo.get_by_post(post_id)

    async def get_replies(self, parent_id: UUID) -> list[Comment]:
        return await self.comment_repo.get_replies(parent_id)
