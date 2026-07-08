from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime
from app.enums.notification_type import NotificationType
class BatchReadRequest(BaseModel):
    ids: list[UUID] | None = None
    all: bool | None = None


class NotificationBase(BaseModel):
    type: NotificationType
    title: str = Field(..., max_length=255)
    content: str = Field(..., max_length=1000)


class NotificationCreate(NotificationBase):
    user_id: UUID


class NotificationResponse(NotificationBase):
    id: UUID
    user_id: UUID
    read: bool
    read_at: Optional[datetime] = None
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
