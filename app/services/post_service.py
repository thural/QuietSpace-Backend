from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.post import Post
from app.repositories.post import PostRepository
from app.schemas.post import PostCreate, PostUpdate, RepostRequest


class PostService:
    def __init__(self, session: AsyncSession, cache_service=None):
        self.post_repo = PostRepository(session)
        self.cache = cache_service

    async def create_post(self, author_id: UUID, post_in: PostCreate) -> Post:
        post = Post(**post_in.model_dump(), author_id=author_id)
        created = await self.post_repo.create(post)
        if self.cache:
            await self.cache.delete(f"post:{created.id}")
        return created

    async def create_repost(self, author_id: UUID, repost_in: RepostRequest) -> Post:
        post = Post(
            text=repost_in.text,
            author_id=author_id,
            repost_text=repost_in.text,
            repost_id=repost_in.post_id,
        )
        created = await self.post_repo.create(post)
        if self.cache:
            await self.cache.delete(f"post:{repost_in.post_id}")
        return created

    async def update_post(self, post_id: UUID, post_in: PostUpdate) -> Post | None:
        post = await self.post_repo.get(post_id)
        if not post:
            return None
        update_data = post_in.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(post, field, value)
        updated = await self.post_repo.update(post)
        if self.cache:
            await self.cache.set(f"post:{post_id}", updated, ttl=300)
        return updated

    async def delete_post(self, post_id: UUID) -> bool:
        await self.post_repo.delete_by_repost_id(post_id)
        result = await self.post_repo.delete(post_id)
        if result and self.cache:
            await self.cache.delete(f"post:{post_id}")
        return result

    async def get_post(self, post_id: UUID) -> Post | None:
        if self.cache:
            cached = await self.cache.get(f"post:{post_id}")
            if cached is not None:
                return cached
        post = await self.post_repo.get(post_id)
        if post and self.cache:
            await self.cache.set(f"post:{post_id}", post, ttl=300)
        return post

    async def get_posts(self, author_id: UUID, limit: int = 20, offset: int = 0) -> list[Post]:
        return await self.post_repo.get_by_author(author_id, limit, offset)
