from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db
from app.config.settings import settings
from app.core.security import create_access_token
from app.enums.role import Role
from app.enums.status_type import StatusType
from app.models.user import User
from app.repositories.user import UserRepository
from app.schemas.user import UserCreate, UserResponse
from app.services.auth_service import AuthService
from app.services.email_service import EmailService
from app.utils.helpers import generate_activation_code

router = APIRouter()
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="auth/login")


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED)
async def register(user_in: UserCreate, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    user = await auth_service.register(
        username=user_in.username,
        email=user_in.email,
        password=user_in.password,
    )
    activation_code = generate_activation_code()
    email_service = EmailService()
    await email_service.send_activation_email(user.email, user.username, activation_code)
    return user


@router.post("/login")
async def login(username: str, password: str, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    result = await auth_service.login(username, password)
    return result


@router.post("/refresh")
async def refresh(token: str, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    result = await auth_service.refresh_token(token)
    return result


@router.post("/logout")
async def logout(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    await auth_service.logout(token)
    return {"message": "Logged out successfully"}
