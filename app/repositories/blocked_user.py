from uuid import UUID
from datetime import datetime
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete, func
from app.models.blocked_user import BlockedUser
from app.models.user import User


class BlockedUserRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def block_user(self, blocker_id: UUID, blocked_id: UUID) -> BlockedUser:
        record = BlockedUser(blocker_id=blocker_id, blocked_id=blocked_id)
        self.session.add(record)
        return record

    async def unblock_user(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        result = await self.session.execute(
            delete(BlockedUser).where(
                BlockedUser.blocker_id == blocker_id,
                BlockedUser.blocked_id == blocked_id,
            )
        )
        return result.rowcount > 0

    async def is_blocked(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        result = await self.session.execute(
            select(BlockedUser).where(
                BlockedUser.blocker_id == blocker_id,
                BlockedUser.blocked_id == blocked_id,
            )
        )
        return result.scalar_one_or_none() is not None

    async def get_blocked_users(
        self, user_id: UUID, page: int = 1, size: int = 20
    ) -> tuple[list[tuple[User, datetime]], int]:
        base_stmt = (
            select(User, BlockedUser.created_at)
            .join(BlockedUser, BlockedUser.blocked_id == User.id)
            .where(BlockedUser.blocker_id == user_id)
        )
        count_stmt = select(func.count()).select_from(base_stmt.subquery())
        total_result = await self.session.execute(count_stmt)
        total = total_result.scalar_one()
        stmt = base_stmt.order_by(BlockedUser.created_at.desc()).offset((page - 1) * size).limit(size)
        result = await self.session.execute(stmt)
        return list(result.all()), total

    async def get_blocked_ids(self, user_id: UUID) -> set[UUID]:
        result = await self.session.execute(
            select(BlockedUser.blocked_id).where(BlockedUser.blocker_id == user_id)
        )
        return {row[0] for row in result.all()}

    async def get_blocker_ids(self, user_id: UUID) -> set[UUID]:
        result = await self.session.execute(
            select(BlockedUser.blocker_id).where(BlockedUser.blocked_id == user_id)
        )
        return {row[0] for row in result.all()}
