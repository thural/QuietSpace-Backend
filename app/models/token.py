from datetime import datetime
from sqlmodel import SQLModel, Field
from sqlalchemy import DateTime
from uuid import UUID
from app.models.base import BaseEntity


class Token(BaseEntity, table=True):
    jti: str = Field(index=True, unique=True, max_length=255)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    expires_at: datetime = Field(sa_type=DateTime(timezone=True))
    revoked: bool = Field(default=False)
