from datetime import datetime, timezone
from sqlmodel import SQLModel, Field, Relationship
from sqlalchemy import DateTime
from uuid import UUID
from app.models.base import BaseEntity


class Poll(BaseEntity, table=True):
    __tablename__ = "polls"

    post_id: UUID = Field(foreign_key="post.id", index=True)
    question: str = Field(max_length=500)
    expires_at: datetime | None = Field(default=None, sa_type=DateTime(timezone=True))

    post: "Post" = Relationship(back_populates="polls")
    options: list["PollOption"] = Relationship(back_populates="poll")


class PollOption(BaseEntity, table=True):
    __tablename__ = "poll_options"

    poll_id: UUID = Field(foreign_key="polls.id", index=True)
    option_text: str = Field(max_length=255)
    vote_count: int = Field(default=0)

    poll: Poll = Relationship(back_populates="options")
    votes: list["PollVote"] = Relationship(back_populates="option")


class PollVote(BaseEntity, table=True):
    __tablename__ = "poll_votes"

    poll_option_id: UUID = Field(foreign_key="poll_options.id", index=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    voted_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), sa_type=DateTime(timezone=True))

    option: PollOption = Relationship(back_populates="votes")
    user: "User" = Relationship(back_populates="poll_votes")
