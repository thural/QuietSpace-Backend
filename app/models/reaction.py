from datetime import datetime
from typing import Optional
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from app.enums.reaction_type import ReactionType
from app.models.base import BaseEntity


class Reaction(BaseEntity, table=True):
    type: ReactionType = Field(index=True)
    user_id: UUID = Field(foreign_key="user.id", index=True)
    post_id: UUID | None = Field(default=None, foreign_key="post.id", index=True)
    comment_id: UUID | None = Field(default=None, foreign_key="comment.id", index=True)

    user: User = Relationship(back_populates="reactions")
    post: Optional["Post"] = Relationship(back_populates="reactions")
    comment: Optional["Comment"] = Relationship(back_populates="reactions")
