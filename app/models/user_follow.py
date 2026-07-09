from datetime import datetime, timezone
from sqlmodel import SQLModel, Field
from sqlalchemy import DateTime
from uuid import UUID


class UserFollow(SQLModel, table=True):
    follower_id: UUID = Field(foreign_key="user.id", primary_key=True)
    following_id: UUID = Field(foreign_key="user.id", primary_key=True)
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), sa_type=DateTime(timezone=True))
