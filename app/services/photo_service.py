import os
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.photo import Photo
from app.repositories.photo import PhotoRepository
from app.schemas.photo import PhotoCreate
from app.repositories.profile_settings import ProfileSettingsRepository


class PhotoService:
    def __init__(self, session: AsyncSession):
        self.photo_repo = PhotoRepository(session)
        self.profile_settings_repo = ProfileSettingsRepository(session)

    async def create_photo(self, photo_in: PhotoCreate) -> Photo:
        photo = Photo(**photo_in.model_dump())
        return await self.photo_repo.create(photo)

    async def get_photos(self, post_id: UUID) -> list[Photo]:
        return await self.photo_repo.get_by_post(post_id)

    async def delete_photo(self, photo_id: UUID) -> bool:
        return await self.photo_repo.delete(photo_id)

    async def delete_profile_photo(self, user_id: UUID) -> bool:
        settings = await self.profile_settings_repo.get_by_user_id(user_id)
        if not settings or not settings.avatar_url:
            return False
        avatar_path = settings.avatar_url
        settings.avatar_url = None
        await self.profile_settings_repo.update(settings)
        if os.path.exists(avatar_path):
            os.remove(avatar_path)
        return True
