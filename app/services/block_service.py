from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.user_follow import UserFollow
from app.models.user import User
from app.repositories.blocked_user import BlockedUserRepository
from app.core.unit_of_work import UnitOfWork


class BlockService:
    def __init__(self, session: AsyncSession, cache_service=None):
        self.session = session
        self.block_repo = BlockedUserRepository(session)
        self.cache = cache_service

    async def block_user(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        if blocker_id == blocked_id:
            return False
        if await self.block_repo.is_blocked(blocker_id, blocked_id):
            return False
        async with UnitOfWork(self.session) as uow:
            await self.block_repo.block_user(blocker_id, blocked_id)
            await self._remove_reciprocal_follows(blocker_id, blocked_id)
            await uow.commit()
        if self.cache:
            await self.cache.delete(f"user:{blocked_id}:followers")
            await self.cache.delete(f"user:{blocker_id}:following")
            await self.cache.delete(f"user:{blocker_id}:blocked")
        return True

    async def unblock_user(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        if blocker_id == blocked_id:
            return False
        result = await self.block_repo.unblock_user(blocker_id, blocked_id)
        if self.cache:
            await self.cache.delete(f"user:{blocker_id}:blocked")
        return result

    async def is_blocked(self, blocker_id: UUID, blocked_id: UUID) -> bool:
        return await self.block_repo.is_blocked(blocker_id, blocked_id)

    async def are_blocked(self, user_id_1: UUID, user_id_2: UUID) -> bool:
        return await self.block_repo.is_blocked(
            user_id_1, user_id_2
        ) or await self.block_repo.is_blocked(user_id_2, user_id_1)

    async def get_blocked_users(
        self, user_id: UUID, cursor: str | None = None, limit: int = 20
    ) -> tuple[list[User], str | None, bool]:
        rows, next_cursor, has_more = await self.block_repo.get_blocked_users(user_id, cursor, limit)
        users = []
        for user, blocked_at in rows:
            user.blocked_at = blocked_at
            users.append(user)
        return users, next_cursor, has_more

    async def get_blocked_ids(self, user_id: UUID) -> set[UUID]:
        return await self.block_repo.get_blocked_ids(user_id)

    async def get_blocker_ids(self, user_id: UUID) -> set[UUID]:
        return await self.block_repo.get_blocker_ids(user_id)

    async def _remove_reciprocal_follows(self, user_a: UUID, user_b: UUID):
        result = await self.session.execute(
            select(UserFollow).where(
                (UserFollow.follower_id == user_a)
                & (UserFollow.following_id == user_b)
                | (UserFollow.follower_id == user_b)
                & (UserFollow.following_id == user_a)
            )
        )
        for follow in result.scalars().all():
            await self.session.delete(follow)
