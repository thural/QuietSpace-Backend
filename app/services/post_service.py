from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.post import Post
from app.repositories.post import PostRepository
from app.repositories.blocked_user import BlockedUserRepository
from app.schemas.post import PostCreate, PostUpdate, RepostRequest


class PostService:
    def __init__(self, session: AsyncSession, cache_service=None):
        self.post_repo = PostRepository(session)
        self.block_repo = BlockedUserRepository(session)
        self.cache = cache_service

    async def create_post(self, author_id: UUID, post_in: PostCreate) -> Post:
        post = Post(**post_in.model_dump(), author_id=author_id)
        created = await self.post_repo.create(post)
        if self.cache:
            await self.cache.delete(f"post:{created.id}")
        return created

    async def create_repost(self, author_id: UUID, repost_in: RepostRequest) -> Post:
        original = await self.post_repo.get(repost_in.post_id)
        if not original:
            raise ValueError("Original post not found")
        if await self.block_repo.is_blocked(author_id, original.author_id):
            raise ValueError("Cannot repost content from a blocked user")
        if await self.block_repo.is_blocked(original.author_id, author_id):
            raise ValueError("You have been blocked by the post author")
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

    async def get_post(self, post_id: UUID, current_user_id: UUID | None = None) -> Post | None:
        if self.cache:
            cached = await self.cache.get(f"post:{post_id}")
            if cached is not None:
                return cached
        post = await self.post_repo.get(post_id)
        if post and current_user_id:
            if await self.block_repo.is_blocked(current_user_id, post.author_id):
                return None
            if await self.block_repo.is_blocked(post.author_id, current_user_id):
                return None
        if post and self.cache:
            await self.cache.set(f"post:{post_id}", post, ttl=300)
        return post

    async def get_posts(self, author_id: UUID, limit: int = 20, offset: int = 0, current_user_id: UUID | None = None) -> list[Post]:
        posts = await self.post_repo.get_by_author(author_id, limit, offset)
        if current_user_id:
            posts = await self._filter_blocked(posts, current_user_id)
        return posts

    async def _filter_blocked(self, posts: list[Post], current_user_id: UUID) -> list[Post]:
        blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
        excluded = blocked_ids | blocker_ids
        return [p for p in posts if p.author_id not in excluded]
