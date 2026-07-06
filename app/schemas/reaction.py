from pydantic import BaseModel, Field
from typing import Optional
from uuid import UUID
from app.enums.reaction_type import ReactionType


class ReactionBase(BaseModel):
    type: ReactionType


class ReactionCreate(ReactionBase):
    post_id: Optional[UUID] = None
    comment_id: Optional[UUID] = None


class ReactionResponse(ReactionBase):
    id: UUID
    user_id: UUID
    post_id: Optional[UUID] = None
    comment_id: Optional[UUID] = None
    created_at: datetime
    updated_at: Optional[datetime] = None

    model_config = {"from_attributes": True}
