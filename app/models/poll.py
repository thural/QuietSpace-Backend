from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class Poll(BaseEntity, table=True):
    post_id: UUID = Field(foreign_key="post.id", index=True)
    question: str = Field(max_length=500)
    expires_at: datetime | None = Field(default=None)
    multiple_choice: bool = Field(default=False)

    post: "Post" = Relationship(back_populates="polls")
    options: list["PollOption"] = Relationship(back_populates="poll")


class PollOption(BaseEntity, table=True):
    poll_id: UUID = Field(foreign_key="poll.id", index=True)
    text: str = Field(max_length=255)
    votes: int = Field(default=0)

    poll: Poll = Relationship(back_populates="options")
