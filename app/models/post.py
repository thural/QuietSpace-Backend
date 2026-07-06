from datetime import datetime
from sqlmodel import SQLModel, Field, Relationship
from uuid import UUID
from typing import Optional
from app.models.base import BaseEntity


class Post(BaseEntity, table=True):
    title: str | None = Field(default=None, max_length=255)
    text: str = Field(max_length=280)
    author_id: UUID = Field(foreign_key="user.id", index=True)
    repost_text: str | None = Field(default=None, max_length=280)
    repost_id: UUID | None = Field(default=None, index=True)

    author: "User" = Relationship(back_populates="posts")
    comments: list["Comment"] = Relationship(back_populates="post")
    reactions: list["Reaction"] = Relationship(back_populates="post")
    photos: list["Photo"] = Relationship(back_populates="post")
    saved_by: list["SavedPost"] = Relationship(back_populates="post")
    polls: list["Poll"] = Relationship(back_populates="post")
