from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship, Index
from sqlalchemy import DateTime
from uuid import UUID
from app.models.base import BaseEntity


class Chat(BaseEntity, table=True):
    __table_args__ = (Index("ix_chat_deleted_at", "deleted_at"),)

    name: str | None = Field(default=None, max_length=255)
    is_group: bool = Field(default=False)
    deleted_at: datetime | None = Field(default=None, sa_type=DateTime(timezone=True))

    messages: list["Message"] = Relationship(back_populates="chat")
    participants: list["ChatParticipant"] = Relationship(back_populates="chat")
