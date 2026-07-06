from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class Message(BaseEntity, table=True):
    chat_id: UUID = Field(foreign_key="chat.id", index=True)
    sender_id: UUID = Field(foreign_key="user.id", index=True)
    recipient_id: UUID = Field(foreign_key="user.id", index=True)
    text: str = Field(max_length=1000)
    read: bool = Field(default=False)
    read_at: datetime | None = Field(default=None)

    chat: Chat = Relationship(back_populates="messages")
    sender: User = Relationship(back_populates="messages_sent")
    recipient: User = Relationship(back_populates="messages_received")
