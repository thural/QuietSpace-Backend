from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class Photo(BaseEntity, table=True):
    post_id: UUID = Field(foreign_key="post.id", index=True)
    filename: str = Field(max_length=255)
    content_type: str = Field(max_length=100)
    size: int
    url: str = Field(max_length=255)
    thumbnail_url: str | None = Field(default=None, max_length=255)

    post: "Post" = Relationship(back_populates="photos")
