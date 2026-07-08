from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.user import User
from app.models.user_follow import UserFollow
from app.repositories.user import UserRepository
from app.repositories.blocked_user import BlockedUserRepository


class UserService:
    def __init__(self, session: AsyncSession, cache_service=None):
        self.session = session
        self.user_repo = UserRepository(session)
        self.block_repo = BlockedUserRepository(session)
        self.cache = cache_service

    async def get_online_users_details(self, online_user_ids: list[UUID]) -> list[User]:
        users = await self.user_repo.get_by_ids(online_user_ids)
        return users

    async def get_user(self, user_id: str) -> User | None:
        uid = UUID(user_id)
        if self.cache:
            cached = await self.cache.get(f"user:{uid}")
            if cached is not None:
                return cached
        user = await self.user_repo.get(uid)
        if user and self.cache:
            await self.cache.set(f"user:{uid}", user, ttl=300)
        return user

    async def delete_user(self, user_id: UUID, actor_id: UUID, is_admin: bool = False) -> bool:
        if user_id != actor_id and not is_admin:
            return False
        deleted = await self.user_repo.soft_delete(user_id)
        if deleted and self.cache:
            await self.cache.delete(f"user:{user_id}")
        return deleted is not None

    async def search_users(self, query: str, current_user_id: UUID | None = None, limit: int = 20) -> list[User]:
        users = await self.user_repo.search(query, limit)
        if current_user_id:
            blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
            blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
            excluded = blocked_ids | blocker_ids
            users = [u for u in users if u.id not in excluded]
        return users

    async def advanced_search(
        self,
        username: str | None = None,
        firstname: str | None = None,
        lastname: str | None = None,
        page: int = 1,
        size: int = 20,
        current_user_id: UUID | None = None,
    ) -> tuple[list[User], int]:
        users, total = await self.user_repo.advanced_search(
            username=username,
            firstname=firstname,
            lastname=lastname,
            page=page,
            size=size,
        )
        if current_user_id:
            blocked_ids = await self.block_repo.get_blocked_ids(current_user_id)
            blocker_ids = await self.block_repo.get_blocker_ids(current_user_id)
            excluded = blocked_ids | blocker_ids
            users = [u for u in users if u.id not in excluded]
        return users, total

    async def follow_user(self, follower_id: UUID, following_id: UUID) -> bool:
        if follower_id == following_id:
            return False
        if await self.block_repo.is_blocked(following_id, follower_id):
            return False
        if await self.block_repo.is_blocked(follower_id, following_id):
            return False
        result = await self.session.execute(
            select(UserFollow).where(
                UserFollow.follower_id == follower_id,
                UserFollow.following_id == following_id,
            )
        )
        existing = result.scalar_one_or_none()
        if existing:
            return False
        follow = UserFollow(follower_id=follower_id, following_id=following_id)
        self.session.add(follow)
        await self.session.commit()
        if self.cache:
            await self.cache.delete(f"user:{following_id}:followers")
            await self.cache.delete(f"user:{follower_id}:following")
        return True

    async def unfollow_user(self, follower_id: UUID, following_id: UUID) -> bool:
        if await self.block_repo.is_blocked(following_id, follower_id):
            return False
        if await self.block_repo.is_blocked(follower_id, following_id):
            return False
        result = await self.session.execute(
            select(UserFollow).where(
                UserFollow.follower_id == follower_id,
                UserFollow.following_id == following_id,
            )
        )
        follow = result.scalar_one_or_none()
        if not follow:
            return False
        await self.session.delete(follow)
        await self.session.commit()
        if self.cache:
            await self.cache.delete(f"user:{following_id}:followers")
            await self.cache.delete(f"user:{follower_id}:following")
        return True

    async def remove_follower(self, user_id: UUID, follower_id: UUID) -> bool:
        if user_id == follower_id:
            return False
        result = await self.user_repo.remove_follower(user_id, follower_id)
        if result:
            await self.session.commit()
            if self.cache:
                await self.cache.delete(f"user:{user_id}:followers")
        return result

    async def get_followers(self, user_id: UUID, page: int = 1, size: int = 20) -> tuple[list[User], int]:
        blocked_ids = await self.block_repo.get_blocked_ids(user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(user_id)
        excluded = blocked_ids | blocker_ids
        followers, total = await self.user_repo.get_followers(user_id, page, size)
        followers = [u for u in followers if u.id not in excluded]
        return followers, total

    async def get_following(self, user_id: UUID, page: int = 1, size: int = 20) -> tuple[list[User], int]:
        blocked_ids = await self.block_repo.get_blocked_ids(user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(user_id)
        excluded = blocked_ids | blocker_ids
        following, total = await self.user_repo.get_following(user_id, page, size)
        following = [u for u in following if u.id not in excluded]
        return following, total
