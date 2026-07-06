from datetime import datetime
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.token import Token
from app.repositories.base import BaseRepository


class TokenRepository(BaseRepository[Token]):
    def __init__(self, session: AsyncSession):
        super().__init__(Token, session)

    async def get_by_jti(self, jti: str) -> Token | None:
        result = await self.session.execute(select(Token).where(Token.jti == jti))
        return result.scalar_one_or_none()

    async def create_token(self, jti: str, user_id: UUID, expires_at: datetime) -> Token:
        token = Token(jti=jti, user_id=user_id, expires_at=expires_at)
        return await self.create(token)


token_repository = TokenRepository
