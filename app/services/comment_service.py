import structlog
from fastapi import HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.comment import Comment
from app.repositories.comment import CommentRepository
from app.repositories.blocked_user import BlockedUserRepository
from app.schemas.comment import CommentCreate, CommentUpdate

logger = structlog.get_logger()


class CommentService:
    MAX_DEPTH = 10

    def __init__(self, session: AsyncSession):
        self.comment_repo = CommentRepository(session)
        self.block_repo = BlockedUserRepository(session)

    async def create_comment(self, author_id: UUID, comment_in: CommentCreate) -> Comment:
        data = comment_in.model_dump()
        if comment_in.parent_id:
            parent = await self.comment_repo.get(comment_in.parent_id)
            if not parent:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Parent comment not found",
                )
            if parent.post_id != comment_in.post_id:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Parent comment does not belong to this post",
                )
            if parent.depth >= self.MAX_DEPTH:
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="Maximum nesting depth exceeded",
                )
            data["depth"] = parent.depth + 1
        comment = Comment(**data, author_id=author_id)
        created = await self.comment_repo.create(comment)
        logger.info("comment_created", comment_id=str(created.id), post_id=str(created.post_id), author_id=str(author_id))
        return created

    async def update_comment(self, comment_id: UUID, comment_in: CommentUpdate, user_id: UUID | None = None) -> Comment | None:
        comment = await self.comment_repo.get(comment_id)
        if not comment:
            return None
        if user_id is not None and comment.author_id != user_id:
            raise ValueError("Not authorized to update this comment")
        update_data = comment_in.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(comment, field, value)
        updated = await self.comment_repo.update(comment)
        logger.info("comment_updated", comment_id=str(comment_id))
        return updated

    async def delete_comment(self, comment_id: UUID) -> bool:
        result = await self.comment_repo.delete(comment_id)
        if result:
            logger.info("comment_deleted", comment_id=str(comment_id))
        return result

    async def get_latest_comment_by_user_on_post(self, user_id: UUID, post_id: UUID) -> Comment | None:
        return await self.comment_repo.get_latest_by_user_on_post(user_id, post_id)

    async def get_comment(self, comment_id: UUID, current_user_id: UUID | None = None) -> Comment | None:
        comment = await self.comment_repo.get(comment_id)
        if not comment:
            return None
        if current_user_id:
            blocked = await self.block_repo.get_blocked_ids(current_user_id)
            blockers = await self.block_repo.get_blocker_ids(current_user_id)
            if comment.author_id in blocked | blockers:
                return None
        return comment

    async def get_comments(
        self, post_id: UUID, cursor: str | None = None, limit: int = 20, current_user_id: UUID | None = None
    ) -> tuple[list[Comment], str | None, bool]:
        comments, next_cursor, has_more = await self.comment_repo.paginate_cursor(
            select(Comment).where(Comment.post_id == post_id, Comment.parent_id == None),
            cursor, limit,
        )
        if current_user_id:
            comments = await self._filter_blocked_comments(comments, current_user_id)
        counts = await self.comment_repo.get_replies_counts([c.id for c in comments])
        for c in comments:
            c.replies_count = counts.get(c.id, 0)
        comments = await self._attach_comment_trees(comments, current_user_id)
        return comments, next_cursor, has_more

    async def get_comments_by_user(
        self, user_id: UUID, cursor: str | None = None, limit: int = 20, current_user_id: UUID | None = None
    ) -> tuple[list[Comment], str | None, bool]:
        comments, next_cursor, has_more = await self.comment_repo.get_by_author(user_id, cursor, limit)
        if current_user_id:
            comments = await self._filter_blocked_comments(comments, current_user_id)
        return comments, next_cursor, has_more

    async def get_replies(
        self, parent_id: UUID, cursor: str | None = None, limit: int = 20, current_user_id: UUID | None = None
    ) -> tuple[list[Comment], str | None, bool]:
        replies, next_cursor, has_more = await self.comment_repo.get_replies(parent_id, cursor, limit)
        if current_user_id:
            replies = await self._filter_blocked_comments(replies, current_user_id)
        counts = await self.comment_repo.get_replies_counts([r.id for r in replies])
        for c in replies:
            c.replies_count = counts.get(c.id, 0)
        return replies, next_cursor, has_more

    async def _attach_comment_trees(self, comments: list[Comment], current_user_id: UUID | None = None) -> list[Comment]:
        if not comments:
            return comments
        root_ids = [c.id for c in comments]
        threads = await self.comment_repo.get_comment_trees(root_ids, self.MAX_DEPTH)
        if not threads:
            return comments
        if current_user_id:
            blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
            blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
            excluded = blocked_ids | blocker_ids
            if excluded:
                threads = [t for t in threads if t.author_id not in excluded]
        thread_dict: dict[UUID, list[Comment]] = {}
        for t in threads:
            if t.parent_id is not None:
                thread_dict.setdefault(t.parent_id, []).append(t)
        for comment in comments:
            comment.replies = self._build_reply_tree(comment.id, thread_dict)
        return comments

    def _build_reply_tree(self, parent_id: UUID, thread_dict: dict[UUID, list[Comment]]) -> list[Comment]:
        children = thread_dict.get(parent_id, [])
        for child in children:
            child.replies = self._build_reply_tree(child.id, thread_dict)
        return children

    async def _filter_blocked_comments(self, comments: list[Comment], current_user_id: UUID) -> list[Comment]:
        blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
        excluded = blocked_ids | blocker_ids
        return [c for c in comments if c.author_id not in excluded]
