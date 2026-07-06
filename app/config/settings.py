from pydantic_settings import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    DATABASE_URL: str = "postgresql+asyncpg://user:password@localhost:5432/quietspace"
    REDIS_URL: str = "redis://localhost:6379/0"
    SECRET_KEY: str = "change-me-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    SMTP_HOST: str = "smtp.example.com"
    SMTP_PORT: int = 587
    SMTP_USER: str = "noreply@example.com"
    SMTP_PASSWORD: str = "your-smtp-password"
    FRONTEND_URL: str = "http://localhost:3000"
    MAX_UPLOAD_SIZE: int = 3 * 1024 * 1024
    DEBUG: bool = False

    class Config:
        env_file = ".env"


settings = Settings()
