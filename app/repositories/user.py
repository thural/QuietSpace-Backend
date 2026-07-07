from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, or_
from sqlalchemy.orm import selectinload, joinedload, Load
from uuid import UUID
from app.models.user import User
from app.models.user_follow import UserFollow
from app.repositories.base import BaseRepository


class UserRepository(BaseRepository[User]):
    def __init__(self, session: AsyncSession):
        super().__init__(User, session)

    async def get_by_email(
        self, email: str, load_options: Optional[list[Load]] = None
    ) -> User | None:
        stmt = select(User).where(User.email == email)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_username(
        self, username: str, load_options: Optional[list[Load]] = None
    ) -> User | None:
        stmt = select(User).where(User.username == username)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_ids(
        self, user_ids: list[UUID], load_options: Optional[list[Load]] = None
    ) -> list[User]:
        stmt = select(User).where(User.id.in_(user_ids))
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def search(
        self, query: str, limit: int = 20, load_options: Optional[list[Load]] = None
    ) -> list[User]:
        stmt = select(User).where(User.username.ilike(f"%{query}%")).limit(limit)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def advanced_search(
        self,
        username: str | None = None,
        firstname: str | None = None,
        lastname: str | None = None,
        page: int = 1,
        size: int = 20,
        load_options: Optional[list[Load]] = None,
    ) -> list[User]:
        conditions = []
        if username:
            conditions.append(User.username.ilike(f"%{username}%"))
        if firstname:
            conditions.append(User.firstname.ilike(f"%{firstname}%"))
        if lastname:
            conditions.append(User.lastname.ilike(f"%{lastname}%"))
        stmt = select(User)
        if conditions:
            stmt = stmt.where(or_(*conditions))
        offset = (page - 1) * size
        stmt = stmt.offset(offset).limit(size)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_with_posts(self, user_id: UUID) -> User | None:
        return await self.get(
            user_id,
            load_options=[selectinload(User.posts), selectinload(User.comments)],
        )

    async def remove_follower(self, user_id: UUID, follower_id: UUID) -> bool:
        stmt = select(UserFollow).where(
            UserFollow.following_id == user_id,
            UserFollow.follower_id == follower_id,
        )
        result = await self.session.execute(stmt)
        follow = result.scalar_one_or_none()
        if not follow:
            return False
        await self.session.delete(follow)
        return True

    async def get_followers(self, user_id: UUID) -> list[User]:
        stmt = (
            select(User)
            .join(UserFollow, UserFollow.follower_id == User.id)
            .where(UserFollow.following_id == user_id)
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_following(self, user_id: UUID) -> list[User]:
        stmt = (
            select(User)
            .join(UserFollow, UserFollow.following_id == User.id)
            .where(UserFollow.follower_id == user_id)
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())



