from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class ProfileSettings(BaseEntity, table=True):
    user_id: UUID = Field(foreign_key="user.id", index=True)
    bio: str | None = Field(default=None, max_length=500)
    avatar_url: str | None = Field(default=None, max_length=255)
    is_public: bool = Field(default=True)

    user: User = Relationship(back_populates="profile_settings")
