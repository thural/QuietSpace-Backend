from sqlalchemy import select, func
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

    async def get_replies(
        self, parent_id: UUID, cursor: str | None = None, limit: int = 20
    ) -> tuple[list[Comment], str | None, bool]:
        stmt = select(Comment).where(Comment.parent_id == parent_id)
        return await self.paginate_cursor(stmt, cursor, limit)

    async def get_thread(self, comment_id: UUID, max_depth: int = 10) -> list[Comment]:
        cte = (
            select(
                Comment.id, Comment.text, Comment.post_id, Comment.author_id,
                Comment.parent_id, Comment.depth, Comment.created_at, Comment.updated_at,
            )
            .where(Comment.id == comment_id)
            .cte(name="comment_tree", recursive=True)
        )
        cte_alias = cte.alias("ct")
        recursive_part = select(
            Comment.id, Comment.text, Comment.post_id, Comment.author_id,
            Comment.parent_id, Comment.depth, Comment.created_at, Comment.updated_at,
        ).where(
            Comment.parent_id == cte_alias.c.id,
            cte_alias.c.depth < max_depth,
        )
        final_cte = cte.union_all(recursive_part)
        stmt = select(Comment).select_from(final_cte).order_by(Comment.depth, Comment.created_at)
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_comment_trees(self, root_ids: list[UUID], max_depth: int = 10) -> list[Comment]:
        if not root_ids:
            return []
        cte = (
            select(
                Comment.id, Comment.text, Comment.post_id, Comment.author_id,
                Comment.parent_id, Comment.depth, Comment.created_at, Comment.updated_at,
            )
            .where(Comment.id.in_(root_ids))
            .cte(name="comment_forest", recursive=True)
        )
        cte_alias = cte.alias("cf")
        recursive_part = select(
            Comment.id, Comment.text, Comment.post_id, Comment.author_id,
            Comment.parent_id, Comment.depth, Comment.created_at, Comment.updated_at,
        ).where(
            Comment.parent_id == cte_alias.c.id,
            cte_alias.c.depth < max_depth,
        )
        final_cte = cte.union_all(recursive_part)
        stmt = select(Comment).select_from(final_cte).order_by(Comment.depth, Comment.created_at)
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_by_author(
        self, author_id: UUID, cursor: str | None = None, limit: int = 20
    ) -> tuple[list[Comment], str | None, bool]:
        stmt = select(Comment).where(Comment.author_id == author_id)
        return await self.paginate_cursor(stmt, cursor, limit)

    async def get_latest_by_user_on_post(self, user_id: UUID, post_id: UUID) -> Comment | None:
        result = await self.session.execute(
            select(Comment)
            .where(Comment.author_id == user_id, Comment.post_id == post_id)
            .order_by(Comment.created_at.desc())
            .limit(1)
        )
        return result.scalar_one_or_none()

    async def get_replies_count(self, parent_id: UUID) -> int:
        result = await self.session.execute(
            select(func.count()).where(Comment.parent_id == parent_id)
        )
        return result.scalar_one()

    async def get_replies_counts(self, parent_ids: list[UUID]) -> dict[UUID, int]:
        if not parent_ids:
            return {}
        result = await self.session.execute(
            select(Comment.parent_id, func.count())
            .where(Comment.parent_id.in_(parent_ids))
            .group_by(Comment.parent_id)
        )
        return dict(result.all())


comment_repository = CommentRepository
