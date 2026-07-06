from datetime import datetime, timedelta
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.notification import Notification
from app.repositories.notification import NotificationRepository
from app.schemas.notification import NotificationCreate


class NotificationService:
    def __init__(self, session: AsyncSession):
        self.notification_repo = NotificationRepository(session)

    async def create_notification(self, notification_in: NotificationCreate) -> Notification:
        notification = Notification(**notification_in.model_dump())
        return await self.notification_repo.create(notification)

    async def get_notifications(self, user_id: UUID, limit: int = 50, offset: int = 0) -> list[Notification]:
        return await self.notification_repo.get_by_user(user_id, limit, offset)

    async def get_unread_count(self, user_id: UUID) -> int:
        return await self.notification_repo.get_unread_count(user_id)

    async def mark_as_read(self, notification_id: UUID) -> Notification | None:
        notification = await self.notification_repo.get(notification_id)
        if notification:
            notification.read = True
            notification.read_at = datetime.utcnow()
            return await self.notification_repo.update(notification)
        return None
