from datetime import datetime, timedelta
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.notification import Notification
from app.repositories.notification import NotificationRepository
from app.schemas.notification import NotificationCreate
from app.celery_app import celery_app


@celery_app.task
def send_email_notification(user_email: str, subject: str, body: str) -> dict:
    from app.services.email_service import EmailService
    import asyncio
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    service = EmailService()
    loop.run_until_complete(service.send_email(user_email, subject, "notification", {"content": body}))
    loop.close()
    return {"sent": True}


class NotificationService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.notification_repo = NotificationRepository(session)

    async def create_notification(self, notification_in: NotificationCreate) -> Notification:
        notification = Notification(**notification_in.model_dump())
        created = await self.notification_repo.create(notification)
        try:
            from app.api.websocket.manager import manager
            await manager.send_to_user(
                created.user_id,
                "new_notification",
                created.model_dump(mode="json"),
            )
        except Exception:
            pass
        return created

    async def get_notifications(
        self, user_id: UUID, limit: int = 50, offset: int = 0, type_filter: Optional[str] = None
    ) -> list[Notification]:
        return await self.notification_repo.get_by_user(user_id, limit, offset, type_filter)

    async def get_unread_count(self, user_id: UUID) -> int:
        return await self.notification_repo.get_unread_count(user_id)

    async def mark_as_read(self, notification_id: UUID) -> Notification | None:
        notification = await self.notification_repo.get(notification_id)
        if notification:
            notification.read = True
            notification.read_at = datetime.utcnow()
            return await self.notification_repo.update(notification)
        return None
