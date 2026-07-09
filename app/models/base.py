from datetime import datetime, timezone
from sqlmodel import SQLModel, Field
from sqlalchemy import DateTime
from uuid import UUID, uuid4
from typing import Optional


class BaseEntity(SQLModel):
    __abstract__ = True
    id: Optional[UUID] = Field(default_factory=uuid4, primary_key=True)
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), sa_type=DateTime(timezone=True))
    updated_at: Optional[datetime] = Field(default_factory=lambda: datetime.now(timezone.utc), sa_type=DateTime(timezone=True))
    version: int = Field(default=0)
    created_by: Optional[UUID] = Field(default=None)
    updated_by: Optional[UUID] = Field(default=None)
