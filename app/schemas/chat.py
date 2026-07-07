from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from datetime import datetime


class ChatBase(BaseModel):
    name: Optional[str] = Field(None, max_length=255)
    is_group: bool = False


class ChatCreate(ChatBase):
    participant_ids: List[UUID]


class ChatUpdate(BaseModel):
    name: Optional[str] = Field(None, max_length=255)
    is_group: Optional[bool] = None


class ChatResponse(ChatBase):
    id: UUID
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
