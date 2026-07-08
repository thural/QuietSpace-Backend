from uuid import UUID
from app.api.websocket.manager import manager
from app.services.notification_service import NotificationService
from sqlalchemy.ext.asyncio import AsyncSession


class WebSocketService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.notification_service = NotificationService(session)

    async def send_new_message_notification(self, recipient_id: UUID, message_text: str):
        notification = await self.notification_service.create_notification(
            user_id=recipient_id,
            type="MESSAGE",
            title="New Message",
            content=message_text,
        )
        await manager.send_to_user(recipient_id, "notification", notification.model_dump())

    async def send_typing_indicator(self, chat_id: UUID, user_id: UUID, is_typing: bool):
        await manager.broadcast_to_chat(
            chat_id, "typing_status", {"user_id": str(user_id), "is_typing": is_typing}
        )
