from datetime import datetime, timezone
from uuid import UUID
from typing import Any
from pydantic import BaseModel
from app.enums.websocket_event_type import WebSocketEventType


class BaseEvent(BaseModel):
    event_type: str
    timestamp: datetime
    actor_id: UUID
    data: dict[str, Any] = {}


class ChatEvent(BaseEvent):
    chat_id: UUID
    message_id: UUID | None = None
    recipient_id: UUID | None = None


class NotificationEvent(BaseEvent):
    notification_id: UUID
    notification_type: str
    recipient_id: UUID


class SystemEvent(BaseEvent):
    severity: str
    message: str


class EventFactory:
    @staticmethod
    def create_chat_event(
        event_type: WebSocketEventType,
        actor_id: UUID,
        chat_id: UUID,
        message_id: UUID | None = None,
        recipient_id: UUID | None = None,
        data: dict[str, Any] | None = None,
    ) -> ChatEvent:
        return ChatEvent(
            event_type=event_type.value,
            timestamp=datetime.now(timezone.utc),
            actor_id=actor_id,
            data=data or {},
            chat_id=chat_id,
            message_id=message_id,
            recipient_id=recipient_id,
        )

    @staticmethod
    def create_notification_event(
        notification_id: UUID,
        notification_type: str,
        actor_id: UUID,
        recipient_id: UUID,
        data: dict[str, Any] | None = None,
    ) -> NotificationEvent:
        return NotificationEvent(
            event_type="NOTIFICATION",
            timestamp=datetime.now(timezone.utc),
            actor_id=actor_id,
            data=data or {},
            notification_id=notification_id,
            notification_type=notification_type,
            recipient_id=recipient_id,
        )

    @staticmethod
    def create_system_event(
        message: str,
        severity: str = "info",
        actor_id: UUID | None = None,
        data: dict[str, Any] | None = None,
    ) -> SystemEvent:
        from uuid import uuid4
        return SystemEvent(
            event_type="SYSTEM",
            timestamp=datetime.now(timezone.utc),
            actor_id=actor_id or uuid4(),
            data=data or {},
            severity=severity,
            message=message,
        )
