from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from typing import Optional
from uuid import UUID


class PollOptionCreate(BaseModel):
    option_text: str = Field(..., max_length=255)


class PollCreate(BaseModel):
    question: str = Field(..., max_length=500)
    expires_at: Optional[datetime] = None
    options: list[PollOptionCreate] = Field(..., min_length=2, max_length=10)


class PollOptionResponse(BaseModel):
    id: UUID
    option_text: str
    vote_count: int
    poll_id: UUID
    model_config = ConfigDict(from_attributes=True)


class PollResponse(BaseModel):
    id: UUID
    post_id: UUID
    question: str
    expires_at: Optional[datetime] = None
    options: list[PollOptionResponse] = []
    model_config = ConfigDict(from_attributes=True)


class VoteRequest(BaseModel):
    poll_option_id: UUID
