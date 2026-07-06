from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.post import Post
from app.repositories.post import PostRepository
from app.schemas.post import PostCreate, PostUpdate


class PostService:
    def __init__(self, session: AsyncSession):
        self.post_repo = PostRepository(session)

    async def create_post(self, author_id: UUID, post_in: PostCreate) -> Post:
        post = Post(**post_in.model_dump(), author_id=author_id)
        return await self.post_repo.create(post)

    async def update_post(self, post_id: UUID, post_in: PostUpdate) -> Post | None:
        post = await self.post_repo.get(post_id)
        if not post:
            return None
        update_data = post_in.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(post, field, value)
        return await self.post_repo.update(post)

    async def delete_post(self, post_id: UUID) -> bool:
        return await self.post_repo.delete(post_id)

    async def get_post(self, post_id: UUID) -> Post | None:
        return await self.post_repo.get(post_id)

    async def get_posts(self, author_id: UUID, limit: int = 20, offset: int = 0) -> list[Post]:
        return await self.post_repo.get_by_author(author_id, limit, offset)
