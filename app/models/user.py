from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship, Index
from sqlalchemy import DateTime
from uuid import UUID
from app.enums.role import Role
from app.enums.status_type import StatusType
from app.models.base import BaseEntity
from app.models.user_follow import UserFollow


class User(BaseEntity, table=True):
    __table_args__ = (Index("ix_user_deleted_at", "deleted_at"),)

    username: str = Field(index=True, unique=True, max_length=32)
    email: str = Field(index=True, unique=True, max_length=255)
    password_hash: str = Field(max_length=255)
    firstname: str | None = Field(default=None, max_length=255, index=True)
    lastname: str | None = Field(default=None, max_length=255, index=True)
    date_of_birth: datetime | None = Field(default=None, sa_type=DateTime(timezone=True))
    role: Role = Field(default=Role.USER)
    status: StatusType = Field(default=StatusType.ACTIVE)
    enabled: bool = Field(default=True)
    activation_code: str | None = Field(default=None, max_length=255)
    activation_code_expires_at: datetime | None = Field(default=None, sa_type=DateTime(timezone=True))
    deleted_at: datetime | None = Field(default=None, sa_type=DateTime(timezone=True))

    profile_settings: list["ProfileSettings"] = Relationship(back_populates="user")
    posts: list["Post"] = Relationship(back_populates="author")
    comments: list["Comment"] = Relationship(back_populates="author")
    messages_sent: list["Message"] = Relationship(
        back_populates="sender",
        sa_relationship_kwargs={"foreign_keys": "Message.sender_id"}
    )
    messages_received: list["Message"] = Relationship(
        back_populates="recipient",
        sa_relationship_kwargs={"foreign_keys": "Message.recipient_id"}
    )
    reactions: list["Reaction"] = Relationship(back_populates="user")
    notifications: list["Notification"] = Relationship(back_populates="user")
    blocked_users: list["BlockedUser"] = Relationship(
        back_populates="blocker",
        sa_relationship_kwargs={"foreign_keys": "BlockedUser.blocker_id"}
    )
    blocked_by: list["BlockedUser"] = Relationship(
        back_populates="blocked",
        sa_relationship_kwargs={"foreign_keys": "BlockedUser.blocked_id"}
    )
    poll_votes: list["PollVote"] = Relationship(back_populates="user")

