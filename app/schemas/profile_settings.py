from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from uuid import UUID


class ProfileSettingsBase(BaseModel):
    bio: Optional[str] = Field(None, max_length=500)
    avatar_url: Optional[str] = Field(None, max_length=255)
    is_public: bool = True


class ProfileSettingsUpdate(BaseModel):
    bio: Optional[str] = None
    avatar_url: Optional[str] = None
    is_public: Optional[bool] = None


class ProfileSettingsResponse(ProfileSettingsBase):
    id: UUID
    user_id: UUID

    model_config = ConfigDict(from_attributes=True)
