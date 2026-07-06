from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.enums.role import Role
from app.enums.status_type import StatusType
from app.models.base import BaseEntity


class User(BaseEntity, table=True):
    username: str = Field(index=True, unique=True, max_length=32)
    email: str = Field(index=True, unique=True, max_length=255)
    password_hash: str = Field(max_length=255)
    firstname: str | None = Field(default=None, max_length=255)
    lastname: str | None = Field(default=None, max_length=255)
    date_of_birth: datetime | None = Field(default=None)
    role: Role = Field(default=Role.USER)
    status: StatusType = Field(default=StatusType.ACTIVE)
    enabled: bool = Field(default=True)

    profile_settings: list["ProfileSettings"] = Relationship(back_populates="user")
    posts: list["Post"] = Relationship(back_populates="author")
    comments: list["Comment"] = Relationship(back_populates="author")
    messages_sent: list["Message"] = Relationship(back_populates="sender")
    messages_received: list["Message"] = Relationship(back_populates="recipient")
    reactions: list["Reaction"] = Relationship(back_populates="user")
    notifications: list["Notification"] = Relationship(back_populates="user")
