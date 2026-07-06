from datetime import datetime
from typing import Optional
from sqlmodel import SQLModel, Field, Relationship, Index
from uuid import UUID
from app.models.base import BaseEntity


class Comment(BaseEntity, table=True):
    __table_args__ = (
        Index("ix_comment_post_parent", "post_id", "parent_id"),
    )

    text: str = Field(max_length=280)
    post_id: UUID = Field(foreign_key="post.id", index=True)
    author_id: UUID = Field(foreign_key="user.id", index=True)
    parent_id: UUID | None = Field(default=None, foreign_key="comment.id", index=True)
    depth: int = Field(default=0, nullable=False)

    post: Post = Relationship(back_populates="comments")
    author: User = Relationship(back_populates="comments")
    parent: Optional["Comment"] = Relationship(
        back_populates="replies",
        sa_relationship_kwargs={"remote_side": lambda: [Comment.id]}
    )
    replies: list["Comment"] = Relationship(back_populates="parent")
    reactions: list["Reaction"] = Relationship(back_populates="comment")
