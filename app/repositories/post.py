from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from app.models.post import Post
from app.models.saved_post import SavedPost
from app.repositories.base import BaseRepository


class PostRepository(BaseRepository[Post]):
    def __init__(self, session: AsyncSession):
        super().__init__(Post, session)

    async def get_by_author(self, author_id: UUID, limit: int = 20, offset: int = 0) -> list[Post]:
        result = await self.session.execute(
            select(Post)
            .where(Post.author_id == author_id)
            .order_by(Post.created_at.desc())
            .limit(limit)
            .offset(offset)
        )
        return result.scalars().all()

    async def get_saved_paginated(self, user_id: UUID, cursor: str | None = None, limit: int = 20) -> tuple[list[Post], str | None, bool]:
        stmt = (
            select(Post)
            .join(SavedPost, SavedPost.post_id == Post.id)
            .where(SavedPost.user_id == user_id)
        )
        return await self.paginate_cursor(stmt, cursor, limit)

    async def search(self, query: str, limit: int = 20) -> list[Post]:
        result = await self.session.execute(
            select(Post)
            .where(Post.text.ilike(f"%{query}%"))
            .limit(limit)
        )
        return result.scalars().all()

    async def delete_by_repost_id(self, post_id: UUID) -> None:
        await self.session.execute(
            Post.__table__.delete().where(Post.repost_id == post_id)
        )


post_repository = PostRepository
