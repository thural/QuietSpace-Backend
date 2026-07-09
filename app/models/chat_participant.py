from datetime import datetime, timezone
from sqlmodel import SQLModel, Field, Relationship
from sqlalchemy import DateTime
from uuid import UUID
from app.models.base import BaseEntity


class ChatParticipant(BaseEntity, table=True):
    chat_id: UUID = Field(foreign_key="chat.id", index=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    joined_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), sa_type=DateTime(timezone=True))

    chat: "Chat" = Relationship(back_populates="participants")
