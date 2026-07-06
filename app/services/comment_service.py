from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.comment import Comment
from app.repositories.comment import CommentRepository
from app.repositories.blocked_user import BlockedUserRepository
from app.schemas.comment import CommentCreate, CommentUpdate


class CommentService:
    def __init__(self, session: AsyncSession):
        self.comment_repo = CommentRepository(session)
        self.block_repo = BlockedUserRepository(session)

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

    async def get_comments(
        self, post_id: UUID, cursor: str | None = None, limit: int = 20, current_user_id: UUID | None = None
    ) -> tuple[list[Comment], str | None, bool]:
        comments, next_cursor, has_more = await self.comment_repo.get_replies(post_id, cursor, limit)
        if current_user_id:
            comments = await self._filter_blocked_comments(comments, current_user_id)
        for c in comments:
            c.replies_count = await self.comment_repo.get_replies_count(c.id)
        return comments, next_cursor, has_more

    async def get_replies(
        self, parent_id: UUID, cursor: str | None = None, limit: int = 20, current_user_id: UUID | None = None
    ) -> tuple[list[Comment], str | None, bool]:
        replies, next_cursor, has_more = await self.comment_repo.get_replies(parent_id, cursor, limit)
        if current_user_id:
            replies = await self._filter_blocked_comments(replies, current_user_id)
        for c in replies:
            c.replies_count = await self.comment_repo.get_replies_count(c.id)
        return replies, next_cursor, has_more

    async def _filter_blocked_comments(self, comments: list[Comment], current_user_id: UUID) -> list[Comment]:
        blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
        excluded = blocked_ids | blocker_ids
        return [c for c in comments if c.author_id not in excluded]
