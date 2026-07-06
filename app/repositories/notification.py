from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.notification import Notification
from app.repositories.base import BaseRepository


class NotificationRepository(BaseRepository[Notification]):
    def __init__(self, session: AsyncSession):
        super().__init__(Notification, session)

    async def get_by_user(
        self, user_id: UUID, limit: int = 50, offset: int = 0, type_filter: str | None = None
    ) -> list[Notification]:
        stmt = select(Notification).where(Notification.user_id == user_id)
        if type_filter:
            stmt = stmt.where(Notification.type == type_filter)
        stmt = stmt.order_by(Notification.created_at.desc()).limit(limit).offset(offset)
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_unread_count(self, user_id: UUID) -> int:
        result = await self.session.execute(
            select(Notification)
            .where(Notification.user_id == user_id, Notification.read == False)
        )
        return len(result.scalars().all())


notification_repository = NotificationRepository
