from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.enums.notification_type import NotificationType
from app.models.base import BaseEntity


class Notification(BaseEntity, table=True):
    user_id: UUID = Field(foreign_key="user.id", index=True)
    type: NotificationType = Field(index=True)
    title: str = Field(max_length=255)
    content: str = Field(max_length=1000)
    read: bool = Field(default=False)
    read_at: datetime | None = Field(default=None)

    user: User = Relationship(back_populates="notifications")
