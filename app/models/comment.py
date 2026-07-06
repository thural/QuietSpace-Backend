from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.models.base import BaseEntity


class Comment(BaseEntity, table=True):
    text: str = Field(max_length=280)
    post_id: UUID = Field(foreign_key="post.id", index=True)
    author_id: UUID = Field(foreign_key="user.id", index=True)
    parent_id: UUID | None = Field(default=None, foreign_key="comment.id", index=True)

    post: Post = Relationship(back_populates="comments")
    author: User = Relationship(back_populates="comments")
    parent: "Comment | None" = Relationship(back_populates="replies")
    replies: list["Comment"] = Relationship(back_populates="parent")
    reactions: list["Reaction"] = Relationship(back_populates="comment")
