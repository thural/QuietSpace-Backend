from uuid import UUID
from sqlmodel import Field, Relationship, UniqueConstraint
from app.models.base import BaseEntity


class BlockedUser(BaseEntity, table=True):
    __tablename__ = "blocked_users"

    blocker_id: UUID = Field(foreign_key="user.id", nullable=False, index=True)
    blocked_id: UUID = Field(foreign_key="user.id", nullable=False, index=True)

    blocker: "User" = Relationship(
        back_populates="blocked_users",
        sa_relationship_kwargs={"foreign_keys": "BlockedUser.blocker_id"}
    )
    blocked: "User" = Relationship(
        back_populates="blocked_by",
        sa_relationship_kwargs={"foreign_keys": "BlockedUser.blocked_id"}
    )

    __table_args__ = (
        UniqueConstraint("blocker_id", "blocked_id", name="uq_blocked_user_pair"),
    )
