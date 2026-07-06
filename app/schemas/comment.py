from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from datetime import datetime


class CommentBase(BaseModel):
    text: str = Field(..., min_length=1, max_length=280)


class CommentCreate(CommentBase):
    post_id: UUID
    parent_id: Optional[UUID] = None


class CommentUpdate(BaseModel):
    text: Optional[str] = Field(None, min_length=1, max_length=280)


class CommentResponse(CommentBase):
    id: UUID
    post_id: UUID
    author_id: UUID
    parent_id: Optional[UUID] = None
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
