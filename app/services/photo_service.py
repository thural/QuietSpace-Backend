from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.photo import Photo
from app.repositories.photo import PhotoRepository
from app.schemas.photo import PhotoCreate


class PhotoService:
    def __init__(self, session: AsyncSession):
        self.photo_repo = PhotoRepository(session)

    async def create_photo(self, photo_in: PhotoCreate) -> Photo:
        photo = Photo(**photo_in.model_dump())
        return await self.photo_repo.create(photo)

    async def get_photos(self, post_id: UUID) -> list[Photo]:
        return await self.photo_repo.get_by_post(post_id)

    async def delete_photo(self, photo_id: UUID) -> bool:
        return await self.photo_repo.delete(photo_id)
