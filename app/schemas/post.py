from pydantic import BaseModel, Field, field_validator, model_validator
from typing import Optional
from uuid import UUID
from datetime import datetime


class PostBase(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    text: str = Field(..., min_length=1, max_length=280)

    @field_validator("text")
    @classmethod
    def sanitize_text(cls, v: str) -> str:
        banned_words = ["spam", "abuse"]
        if any(word in v.lower() for word in banned_words):
            raise ValueError("Text contains prohibited content")
        return v.strip()

    @model_validator(mode="after")
    def validate_content_length(self) -> "PostBase":
        if self.text and len(self.text) > 280:
            raise ValueError("Post exceeds 280 character limit")
        return self


class PostCreate(PostBase):
    pass


class PostUpdate(BaseModel):
    title: Optional[str] = Field(None, max_length=255)
    text: Optional[str] = Field(None, min_length=1, max_length=280)


class RepostRequest(BaseModel):
    text: str = Field(..., min_length=1, max_length=280)
    post_id: UUID


class RepostResponse(BaseModel):
    id: UUID
    text: str | None = None
    author_id: UUID
    username: str
    parent_id: UUID | None = None
    is_repost: bool = True

    model_config = {"from_attributes": True}


class PostResponse(PostBase):
    id: UUID
    author_id: UUID
    created_at: datetime
    updated_at: Optional[datetime] = None
    repost: Optional["PostResponse"] = None
    repost_text: Optional[str] = None
    repost_id: Optional[UUID] = None

    model_config = {"from_attributes": True}
