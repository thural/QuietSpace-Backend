from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from app.models.profile_settings import ProfileSettings
from app.repositories.base import BaseRepository


class ProfileSettingsRepository(BaseRepository[ProfileSettings]):
    def __init__(self, session: AsyncSession):
        super().__init__(ProfileSettings, session)

    async def get_by_user_id(self, user_id: UUID) -> ProfileSettings | None:
        result = await self.session.execute(
            select(ProfileSettings).where(ProfileSettings.user_id == user_id)
        )
        return result.scalar_one_or_none()
