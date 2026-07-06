from uuid import UUID
from datetime import datetime
from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, delete, tuple_
from app.models.blocked_user import BlockedUser
from app.models.user import User
from app.utils.cursor import encode_cursor, decode_cursor


class BlockedUserRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def block_user(self, blocker_id: UUID, blocked_id: UUID) -> BlockedUser:
        record = BlockedUser(blocker_id=blocker_id, blocked_id=blocked_id)
        self.session.add(record)
        await self.session.commit()
        return record

    async def unblock_user(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        result = await self.session.execute(
            delete(BlockedUser).where(
                BlockedUser.blocker_id == blocker_id,
                BlockedUser.blocked_id == blocked_id,
            )
        )
        await self.session.commit()
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
        self, user_id: UUID, cursor: str | None = None, limit: int = 20
    ) -> tuple[list[tuple[User, datetime]], str | None, bool]:
        stmt = (
            select(User, BlockedUser.created_at)
            .join(BlockedUser, BlockedUser.blocked_id == User.id)
            .where(BlockedUser.blocker_id == user_id)
        )
        if cursor:
            cursor_ts, cursor_id = decode_cursor(cursor)
            stmt = stmt.where(
                tuple_(BlockedUser.created_at, BlockedUser.id) < tuple_(cursor_ts, cursor_id)
            )
        stmt = stmt.order_by(BlockedUser.created_at.desc(), BlockedUser.id.desc()).limit(limit + 1)
        result = await self.session.execute(stmt)
        rows = list(result.all())
        has_more = len(rows) > limit
        rows = rows[:limit]
        next_cursor = None
        if has_more and rows:
            last_user, last_created_at = rows[-1]
            last_block = await self.session.execute(
                select(BlockedUser).where(
                    BlockedUser.blocker_id == user_id,
                    BlockedUser.blocked_id == last_user.id,
                )
            )
            last_block_record = last_block.scalar_one_or_none()
            if last_block_record:
                next_cursor = encode_cursor(last_block_record.created_at, last_block_record.id)
        return rows, next_cursor, has_more

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
