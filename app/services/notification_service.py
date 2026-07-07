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

    async def create_notification(
        self, notification_in: NotificationCreate, actor_id: UUID | None = None
    ) -> Notification:
        notification = Notification(**notification_in.model_dump())
        created = await self.notification_repo.create(notification)
        try:
            from app.models.websocket_event import EventFactory
            from app.api.websocket.manager import manager

            event = EventFactory.create_notification_event(
                notification_id=created.id,
                notification_type=created.type.value,
                actor_id=actor_id or created.user_id,
                recipient_id=created.user_id,
                data={"title": created.title, "content": created.content},
            )
            await manager.send_to_user(
                created.user_id, "notification", event.model_dump(mode="json")
            )
            unread = await self.get_unread_count(created.user_id)
            await manager.send_to_user(
                created.user_id, "unread_count", {"count": unread}
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

    async def mark_as_read(
        self, notification_id: UUID, actor_id: UUID | None = None
    ) -> Notification | None:
        notification = await self.notification_repo.get(notification_id)
        if notification:
            notification.read = True
            notification.read_at = datetime.utcnow()
            result = await self.notification_repo.update(notification)
            try:
                from app.models.websocket_event import EventFactory
                from app.api.websocket.manager import manager

                event = EventFactory.create_notification_read_event(
                    notification_id=result.id,
                    notification_type=result.type.value,
                    actor_id=actor_id or result.user_id,
                    recipient_id=result.user_id,
                    data={"title": result.title},
                )
                await manager.send_to_user(
                    result.user_id, "notification", event.model_dump(mode="json")
                )
                unread = await self.get_unread_count(result.user_id)
                await manager.send_to_user(
                    result.user_id, "unread_count", {"count": unread}
                )
            except Exception:
                pass
            return result
        return None
