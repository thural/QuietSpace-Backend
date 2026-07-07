from fastapi import APIRouter, Depends, HTTPException, status, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.enums import Role
from app.models.user import User
from app.core.rate_limiter import limiter, SENSITIVE_LIMIT
from app.repositories.user import UserRepository

router = APIRouter()


@router.get("/users")
@limiter.limit(SENSITIVE_LIMIT)
async def list_users(request: Request, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if current_user.role != Role.ADMIN:
        raise HTTPException(status_code=403, detail="Not authorized")
    repo = UserRepository(db)
    users = await repo.get_all()
    return users


@router.delete("/users/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
@limiter.limit(SENSITIVE_LIMIT)
async def delete_user(request: Request, user_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if current_user.role != Role.ADMIN:
        raise HTTPException(status_code=403, detail="Not authorized")
    repo = UserRepository(db)
    user = await repo.get(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    await repo.delete(user_id)


@router.put("/users/{user_id}/disable")
@limiter.limit(SENSITIVE_LIMIT)
async def disable_user(request: Request, user_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if current_user.role != Role.ADMIN:
        raise HTTPException(status_code=403, detail="Not authorized")
    repo = UserRepository(db)
    user = await repo.get(user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    user.enabled = False
    await repo.update(user)
    return user
