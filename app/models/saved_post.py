from datetime import datetime, timezone
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class SavedPost(BaseEntity, table=True):
    user_id: UUID = Field(foreign_key="user.id", index=True)
    post_id: UUID = Field(foreign_key="post.id", index=True)
    saved_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))

    post: "Post" = Relationship(back_populates="saved_by")
