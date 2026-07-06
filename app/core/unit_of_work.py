from sqlalchemy.ext.asyncio import AsyncSession
from app.models.websocket_event import BaseEvent, ChatEvent, NotificationEvent, SystemEvent


class UnitOfWork:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.events: list[BaseEvent] = []

    async def __aenter__(self):
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if exc_type is not None:
            await self.rollback()
            return False
        await self.commit()
        return True

    async def commit(self):
        await self.session.commit()
        await self._flush_events()

    async def rollback(self):
        await self.session.rollback()
        self.events.clear()

    def add_event(self, event: BaseEvent):
        self.events.append(event)

    async def _flush_events(self):
        from app.api.websocket.manager import manager
        from app.api.websocket.socketio import socketio
        for event in self.events:
            payload = event.model_dump(mode="json")
            if isinstance(event, ChatEvent):
                await manager.broadcast_to_chat(event.chat_id, "chat_event", payload)
                if event.recipient_id:
                    await manager.send_to_user(event.recipient_id, "chat_event", payload)
            elif isinstance(event, NotificationEvent):
                await manager.send_to_user(event.recipient_id, "notification", payload)
            elif isinstance(event, SystemEvent):
                await socketio.emit("system", payload)
        self.events.clear()
