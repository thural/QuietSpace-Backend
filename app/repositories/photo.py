from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.photo import Photo
from app.repositories.base import BaseRepository


class PhotoRepository(BaseRepository[Photo]):
    def __init__(self, session: AsyncSession):
        super().__init__(Photo, session)

    async def get_by_post(self, post_id: UUID) -> list[Photo]:
        result = await self.session.execute(
            select(Photo).where(Photo.post_id == post_id)
        )
        return result.scalars().all()


photo_repository = PhotoRepository
