from datetime import datetime, timedelta
from jose import JWTError, jwt
from sqlalchemy.ext.asyncio import AsyncSession
from app.config.settings import settings
from app.core.security import hash_password, verify_password, create_access_token, decode_token
from app.models.user import User
from app.models.token import Token
from app.repositories.user import UserRepository
from app.repositories.token import TokenRepository


class AuthService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.user_repo = UserRepository(session)
        self.token_repo = TokenRepository(session)

    async def register(self, username: str, email: str, password: str) -> User:
        existing = await self.user_repo.get_by_email(email)
        if existing:
            raise ValueError("Email already registered")
        existing = await self.user_repo.get_by_username(username)
        if existing:
            raise ValueError("Username already taken")
        user = User(
            username=username,
            email=email,
            password_hash=hash_password(password),
        )
        return await self.user_repo.create(user)

    async def login(self, username: str, password: str) -> dict:
        user = await self.user_repo.get_by_username(username)
        if not user or not verify_password(password, user.password_hash):
            raise ValueError("Invalid credentials")
        if not user.enabled:
            raise ValueError("Account is disabled")
        access_token = create_access_token({"sub": user.email})
        return {"access_token": access_token, "token_type": "bearer", "user": user}

    async def refresh_token(self, token: str) -> dict:
        try:
            payload = decode_token(token)
            user = await self.user_repo.get_by_email(payload.get("sub"))
            if not user:
                raise ValueError("Invalid token")
            new_token = create_access_token({"sub": user.email})
            return {"access_token": new_token, "token_type": "bearer"}
        except JWTError:
            raise ValueError("Invalid token")

    async def logout(self, jti: str) -> None:
        token = await self.token_repo.get_by_jti(jti)
        if token:
            token.revoked = True
            await self.token_repo.update(token)
