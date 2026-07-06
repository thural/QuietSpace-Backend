from typing import Annotated
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession

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
    token = credentials.credentials
    try:
        payload = decode_token(token)
        user_email = payload.get("sub")
        if not user_email:
            raise HTTPException(status_code=401, detail="Invalid token")
        from app.main import app
        async with app.state.async_session() as session:
            repo = UserRepository(session)
            user = await repo.get_by_email(user_email)
            if not user:
                raise HTTPException(status_code=401, detail="Invalid token")
            return user
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid token")
