from pydantic import BaseModel, Field
from typing import Optional, List
from uuid import UUID
from datetime import datetime


class PhotoBase(BaseModel):
    filename: str = Field(..., max_length=255)
    content_type: str = Field(..., max_length=100)
    size: int
    url: str = Field(..., max_length=255)
    thumbnail_url: Optional[str] = Field(None, max_length=255)


class PhotoCreate(PhotoBase):
    post_id: UUID


class PhotoResponse(PhotoBase):
    id: UUID
    post_id: UUID
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
