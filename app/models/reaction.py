from datetime import datetime
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
    post: Post | None = Relationship(back_populates="reactions")
    comment: Comment | None = Relationship(back_populates="reactions")
