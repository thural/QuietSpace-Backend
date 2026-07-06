from pydantic import BaseModel, Field, ConfigDict
from datetime import datetime
from typing import Optional
from uuid import UUID


class UserBase(BaseModel):
    username: str = Field(..., min_length=3, max_length=32)
    email: str = Field(..., max_length=255)
    firstname: Optional[str] = Field(None, max_length=255)
    lastname: Optional[str] = Field(None, max_length=255)
    date_of_birth: Optional[datetime] = None


class UserCreate(UserBase):
    password: str = Field(..., min_length=8, max_length=128)


class UserUpdate(BaseModel):
    firstname: Optional[str] = None
    lastname: Optional[str] = None
    date_of_birth: Optional[datetime] = None


class UserResponse(UserBase):
    id: UUID
    role: str
    enabled: bool
    created_at: datetime

    model_config = ConfigDict(from_attributes=True)
