from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from app.models.post import Post
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

    async def search(self, query: str, limit: int = 20) -> list[Post]:
        result = await self.session.execute(
            select(Post)
            .where(Post.text.ilike(f"%{query}%"))
            .limit(limit)
        )
        return result.scalars().all()


post_repository = PostRepository
