from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.photo import Photo
from app.repositories.base import BaseRepository


class PhotoRepository(BaseRepository[Photo]):
    def __init__(self, session: AsyncSession):
        super().__init__(Photo, session)

    async def get_by_post(self, post_id: UUID, cursor: str | None = None, limit: int = 20) -> tuple[list[Photo], str | None, bool]:
        stmt = select(Photo).where(Photo.post_id == post_id)
        return await self.paginate_cursor(stmt, cursor, limit)


photo_repository = PhotoRepository
