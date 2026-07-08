from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status, Body, Request
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.ext.asyncio import AsyncSession
from app.api.deps import get_db
from app.config.settings import settings
from app.core.security import create_access_token
from app.core.rate_limiter import limiter, AUTH_LIMIT, RESEND_CODE_LIMIT
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


@router.post("/register", response_model=UserResponse, status_code=status.HTTP_201_CREATED, summary="Register a new user")
@limiter.limit(AUTH_LIMIT)
async def register(request: Request, user_in: UserCreate, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    try:
        user = await auth_service.register(
            username=user_in.username,
            email=user_in.email,
            password=user_in.password,
        )
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    email_service = EmailService()
    await email_service.send_activation_email(user.email, user.username, user.activation_code)
    return user


@router.post("/login", summary="Authenticate user and return tokens")
@limiter.limit(AUTH_LIMIT)
async def login(request: Request, username: str = Body(...), password: str = Body(...), db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    try:
        result = await auth_service.login(username, password)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))
    return result


@router.post("/refresh", summary="Refresh access token")
@limiter.limit(AUTH_LIMIT)
async def refresh(request: Request, token: str = Body(..., embed=True), db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    try:
        result = await auth_service.refresh_token(token)
    except ValueError as e:
        raise HTTPException(status_code=401, detail=str(e))
    return result


@router.post("/logout", summary="Logout user and invalidate token")
@limiter.limit(AUTH_LIMIT)
async def logout(request: Request, token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    await auth_service.logout(token)
    return {"message": "Logged out successfully"}


@router.post("/activate-account", summary="Activate user account with activation code")
@limiter.limit(AUTH_LIMIT)
async def activate_account(request: Request, code: str = Body(..., embed=True), db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    user = await auth_service.activate_account(code)
    if not user:
        raise HTTPException(status_code=400, detail="Invalid or expired activation code")
    return {"message": "Account activated successfully"}


@router.post("/resend-code", summary="Resend activation code to email")
@limiter.limit(RESEND_CODE_LIMIT)
async def resend_code(
    request: Request,
    email: str = Body(..., embed=True),
    db: AsyncSession = Depends(get_db),
):
    redis = request.app.state.redis
    rate_key = f"resend_code:{email}"
    attempts = await redis.get(rate_key)
    if attempts and int(attempts) >= 3:
        raise HTTPException(status_code=429, detail="Too many requests. Please try again later.")
    auth_service = AuthService(db)
    try:
        user = await auth_service.resend_activation_email(email)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    email_service = EmailService()
    await email_service.send_activation_email(user.email, user.username, user.activation_code)
    pipe = redis.pipeline()
    pipe.incr(rate_key)
    pipe.expire(rate_key, 300)
    await pipe.execute()
    return {"message": "Activation code resent successfully"}
