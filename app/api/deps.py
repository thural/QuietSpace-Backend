from typing import Annotated
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from redis.asyncio import Redis
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.user import User

security = HTTPBearer()


async def get_db():
    from app.main import app
    async with app.state.async_session() as session:
        try:
            yield session
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()


async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    from app.core.security import decode_token
    from app.repositories.user import UserRepository
    from app.repositories.token import TokenRepository
    token = credentials.credentials
    try:
        payload = decode_token(token)
        user_email = payload.get("sub")
        jti = payload.get("jti")
        if not user_email:
            raise HTTPException(status_code=401, detail="Invalid token")
        from app.main import app
        async with app.state.async_session() as session:
            if jti:
                token_repo = TokenRepository(session)
                stored = await token_repo.get_by_jti(jti)
                if stored and stored.revoked:
                    raise HTTPException(status_code=401, detail="Token has been revoked")
            repo = UserRepository(session)
            user = await repo.get_by_email(user_email)
            if not user:
                raise HTTPException(status_code=401, detail="Invalid token")
            return user
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token")


async def get_optional_current_user(
    credentials: HTTPAuthorizationCredentials | None = Depends(HTTPBearer(auto_error=False)),
) -> User | None:
    if not credentials:
        return None
    try:
        return await get_current_user(credentials)
    except HTTPException:
        return None


async def get_redis():
    from app.main import app
    return getattr(app.state, "redis", None)


async def get_cache():
    from app.main import app
    return getattr(app.state, "cache_service", None)


AsyncSessionDep = Annotated[AsyncSession, Depends(get_db)]
CurrentUserDep = Annotated[User, Depends(get_current_user)]
RedisDep = Annotated[Redis, Depends(get_redis)]
