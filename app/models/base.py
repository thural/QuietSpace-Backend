from datetime import datetime
from sqlmodel import SQLModel, Field
from uuid import UUID, uuid4
from typing import Optional


class BaseEntity(SQLModel):
    id: Optional[UUID] = Field(default_factory=uuid4, primary_key=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: Optional[datetime] = Field(default_factory=datetime.utcnow)
    version: int = Field(default=0)
    created_by: Optional[UUID] = Field(default=None)
    updated_by: Optional[UUID] = Field(default=None)
