from datetime import datetime
from sqlmodel import SQLModel, Field
from uuid import UUID


class Token(BaseModel):
    access_token: str
    refresh_token: str | None = None
    token_type: str = "bearer"


class TokenData(BaseModel):
    username: str | None = None


class UserLogin(BaseModel):
    username: str
    password: str


class UserCreate(BaseModel):
    username: str = Field(..., min_length=3, max_length=32)
    email: str = Field(..., max_length=255)
    password: str = Field(..., min_length=8, max_length=128)
    firstname: str | None = Field(default=None, max_length=255)
    lastname: str | None = Field(default=None, max_length=255)


class UserUpdate(BaseModel):
    firstname: str | None = None
    lastname: str | None = None
    date_of_birth: datetime | None = None


class UserResponse(BaseModel):
    id: UUID
    username: str
    email: str
    firstname: str | None
    lastname: str | None
    role: str
    enabled: bool
    created_at: datetime

    model_config = {"from_attributes": True}
