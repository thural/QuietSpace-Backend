from datetime import datetime, timedelta
from uuid import UUID, uuid4
from jose import JWTError, jwt
from sqlalchemy.ext.asyncio import AsyncSession
from app.config.settings import settings
from app.core.security import hash_password, verify_password, create_access_token, decode_token
from app.enums.status_type import StatusType
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
        activation_code = str(uuid4())
        user = User(
            username=username,
            email=email,
            password_hash=hash_password(password),
            activation_code=activation_code,
            activation_code_expires_at=datetime.utcnow() + timedelta(hours=24),
            enabled=False,
            status=StatusType.PENDING,
        )
        return await self.user_repo.create(user)

    async def activate_account(self, code: str) -> User | None:
        from sqlalchemy import select
        result = await self.session.execute(
            select(User).where(User.activation_code == code)
        )
        user = result.scalar_one_or_none()
        if not user:
            return None
        if user.activation_code_expires_at and user.activation_code_expires_at < datetime.utcnow():
            return None
        user.enabled = True
        user.status = StatusType.ACTIVE
        user.activation_code = None
        user.activation_code_expires_at = None
        await self.user_repo.update(user)
        return user

    async def resend_activation_email(self, email: str) -> User:
        user = await self.user_repo.get_by_email(email)
        if not user:
            raise ValueError("User not found")
        if user.enabled and user.status == StatusType.ACTIVE:
            raise ValueError("Account already activated")
        user.activation_code = str(uuid4())
        user.activation_code_expires_at = datetime.utcnow() + timedelta(hours=24)
        await self.user_repo.update(user)
        return user

    async def login(self, username: str, password: str) -> dict:
        user = await self.user_repo.get_by_username(username)
        if not user or not verify_password(password, user.password_hash):
            raise ValueError("Invalid credentials")
        if not user.enabled:
            raise ValueError("Account is disabled")
        access_token = create_access_token({"sub": user.email})
        payload = decode_token(access_token)
        jti = payload.get("jti")
        expires_at = datetime.fromtimestamp(payload.get("exp"))
        await self.token_repo.create_token(jti, user.id, expires_at)
        return {"access_token": access_token, "token_type": "bearer", "user": user}

    async def refresh_token(self, token: str) -> dict:
        try:
            payload = decode_token(token)
            jti = payload.get("jti")
            stored = await self.token_repo.get_by_jti(jti)
            if not stored or stored.revoked:
                raise ValueError("Token has been revoked")
            user = await self.user_repo.get_by_email(payload.get("sub"))
            if not user:
                raise ValueError("Invalid token")
            new_token = create_access_token({"sub": user.email})
            new_payload = decode_token(new_token)
            await self.token_repo.create_token(new_payload.get("jti"), user.id, datetime.fromtimestamp(new_payload.get("exp")))
            return {"access_token": new_token, "token_type": "bearer"}
        except JWTError:
            raise ValueError("Invalid token")

    async def logout(self, token: str) -> None:
        try:
            payload = decode_token(token)
            jti = payload.get("jti")
            stored = await self.token_repo.get_by_jti(jti)
            if stored:
                stored.revoked = True
                await self.token_repo.update(stored)
        except JWTError:
            pass
