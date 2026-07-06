from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime


class MessageBase(BaseModel):
    text: str = Field(..., max_length=1000)


class MessageCreate(MessageBase):
    chat_id: UUID
    recipient_id: UUID


class MessageResponse(MessageBase):
    id: UUID
    chat_id: UUID
    sender_id: UUID
    recipient_id: UUID
    read: bool
    read_at: Optional[datetime] = None
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
