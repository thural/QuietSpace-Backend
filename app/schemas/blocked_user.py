from datetime import datetime
from uuid import UUID
from pydantic import BaseModel


class BlockedUserResponse(BaseModel):
    id: UUID
    username: str
    blocked_at: datetime

    model_config = {"from_attributes": True}
