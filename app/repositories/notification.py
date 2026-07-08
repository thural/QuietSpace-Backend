from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.notification import Notification
from app.repositories.base import BaseRepository


class NotificationRepository(BaseRepository[Notification]):
    def __init__(self, session: AsyncSession):
        super().__init__(Notification, session)

    async def get_by_user(
        self, user_id: UUID, cursor: str | None = None, limit: int = 50, type_filter: str | None = None
    ) -> tuple[list[Notification], str | None, bool]:
        stmt = select(Notification).where(Notification.user_id == user_id)
        if type_filter:
            stmt = stmt.where(Notification.type == type_filter)
        return await self.paginate_cursor(stmt, cursor, limit)

    async def mark_all_as_read(self, user_id: UUID) -> int:
        from datetime import datetime
        result = await self.session.execute(
            select(Notification).where(
                Notification.user_id == user_id, Notification.read == False
            )
        )
        notifications = result.scalars().all()
        now = datetime.utcnow()
        for n in notifications:
            n.read = True
            n.read_at = now
        return len(notifications)

    async def get_unread_count(self, user_id: UUID) -> int:
        result = await self.session.execute(
            select(Notification)
            .where(Notification.user_id == user_id, Notification.read == False)
        )
        return len(result.scalars().all())


notification_repository = NotificationRepository
