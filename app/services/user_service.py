from sqlalchemy.ext.asyncio import AsyncSession
from app.models.user import User
from app.repositories.user import UserRepository


class UserService:
    def __init__(self, session: AsyncSession):
        self.user_repo = UserRepository(session)

    async def get_user(self, user_id: str) -> User | None:
        from uuid import UUID
        return await self.user_repo.get(UUID(user_id))

    async def search_users(self, query: str, limit: int = 20) -> list[User]:
        return await self.user_repo.search(query, limit)
