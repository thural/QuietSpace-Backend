from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.user import User
from app.models.user_follow import UserFollow
from app.repositories.user import UserRepository


class UserService:
    def __init__(self, session: AsyncSession, cache_service=None):
        self.session = session
        self.user_repo = UserRepository(session)
        self.cache = cache_service

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

    async def search_users(self, query: str, limit: int = 20) -> list[User]:
        return await self.user_repo.search(query, limit)

    async def follow_user(self, follower_id: UUID, following_id: UUID) -> bool:
        if follower_id == following_id:
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

    async def get_followers(self, user_id: UUID) -> list[User]:
        if self.cache:
            cached = await self.cache.get(f"user:{user_id}:followers")
            if cached is not None:
                return cached
        followers = await self.user_repo.get_followers(user_id)
        if self.cache:
            await self.cache.set(f"user:{user_id}:followers", followers, ttl=120)
        return followers

    async def get_following(self, user_id: UUID) -> list[User]:
        if self.cache:
            cached = await self.cache.get(f"user:{user_id}:following")
            if cached is not None:
                return cached
        following = await self.user_repo.get_following(user_id)
        if self.cache:
            await self.cache.set(f"user:{user_id}:following", following, ttl=120)
        return following
