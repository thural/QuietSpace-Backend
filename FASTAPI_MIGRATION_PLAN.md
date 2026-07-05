# FastAPI Migration Analysis & Plan

## Current Architecture Summary

**Tech Stack**: Spring Boot 3.3.4 (Java 17), MySQL, JPA/Hibernate, Spring Security + JWT, WebSocket (STOMP), Flyway, SpringDoc OpenAPI

**Core Components**:
- 9 REST controllers (User, Post, Comment, Chat, Message, Notification, Reaction, Photo, Admin)
- 19 business services
- 13 JPA entities with complex relationships
- JWT authentication with token blacklist
- Real-time WebSocket messaging
- Email notifications
- Image processing with Thumbnailator
- Polls and reactions system

**Database**: 13 tables with many-to-many and one-to-many relationships

---

## FastAPI Migration Plan (2026 Best Practices)

### 1. **Core Technology Stack**

```python
# Core Framework
fastapi==0.115.0              # Modern async framework
uvicorn[standard]==0.32.0    # ASGI server with http2 support
pydantic==2.9.0              # v2 for better validation
pydantic-settings==2.6.0     # Settings management

# Database & ORM
sqlalchemy[asyncio]==2.0.35  # Async ORM with 2.0 improvements
asyncmy==0.2.9               # Async MySQL driver
alembic==1.13.3             # Database migrations

# Authentication & Security
passlib[bcrypt]==1.7.4      # Password hashing
python-jose[cryptography]==3.3.0  # JWT handling
python-multipart==0.0.12     # Form data handling
itsdangerous==2.2.0         # Token generation

# WebSocket & Real-time
fastapi-websocket-pubsub==0.1.4  # WebSocket pub/sub
redis==5.1.1                # For WebSocket state & token blacklist

# Email
fastapi-mail==1.4.2         # Async email handling
jinja2==3.1.4               # Email templates

# Image Processing
pillow==10.4.0              # Image processing
python-magic==0.4.27        # File type detection

# API Documentation
fastapi-swagger-ui==0.0.1   # Enhanced Swagger UI

# Testing
pytest==8.3.3               # Modern testing
pytest-asyncio==0.24.0      # Async test support
httpx==0.27.2               # Async HTTP client
pytest-cov==6.0.0           # Coverage

# Development & Quality
black==24.8.0               # Code formatting
ruff==0.6.9                 # Fast linter
mypy==1.11.2                # Type checking
pre-commit==3.8.0           # Git hooks
```

### 2. **Project Structure**

```
quietspace-backend/
├── app/
│   ├── __init__.py
│   ├── main.py                    # FastAPI app factory
│   ├── config/
│   │   ├── __init__.py
│   │   ├── settings.py            # Pydantic Settings
│   │   ├── database.py            # Database session
│   │   ├── redis.py               # Redis client
│   │   └── security.py            # Security constants
│   ├── api/
│   │   ├── __init__.py
│   │   ├── deps.py                # Dependencies (auth, db)
│   │   ├── v1/
│   │   │   ├── __init__.py
│   │   │   ├── router.py           # API router aggregation
│   │   │   ├── auth.py            # Auth endpoints
│   │   │   ├── users.py           # User endpoints
│   │   │   ├── posts.py           # Post endpoints
│   │   │   ├── comments.py        # Comment endpoints
│   │   │   ├── chats.py           # Chat endpoints
│   │   │   ├── messages.py        # Message endpoints
│   │   │   ├── notifications.py   # Notification endpoints
│   │   │   ├── reactions.py       # Reaction endpoints
│   │   │   ├── photos.py          # Photo endpoints
│   │   │   └── admin.py           # Admin endpoints
│   │   └── websocket/
│   │       ├── __init__.py
│   │       ├── manager.py         # WebSocket connection manager
│   │       └── handlers.py        # WebSocket message handlers
│   ├── core/
│   │   ├── __init__.py
│   │   ├── security.py            # JWT, password hashing
│   │   ├── exceptions.py          # Custom exceptions
│   │   ├── middleware.py          # Custom middleware
│   │   └── audit.py               # Audit logging
│   ├── models/
│   │   ├── __init__.py
│   │   ├── base.py                # Base model with audit fields
│   │   ├── user.py                # User model
│   │   ├── post.py                # Post model
│   │   ├── comment.py             # Comment model
│   │   ├── message.py             # Message model
│   │   ├── chat.py                # Chat model
│   │   ├── notification.py        # Notification model
│   │   ├── reaction.py            # Reaction model
│   │   ├── poll.py                # Poll model
│   │   ├── token.py               # Token model
│   │   └── photo.py               # Photo model
│   ├── schemas/
│   │   ├── __init__.py
│   │   ├── user.py                # User DTOs
│   │   ├── post.py                # Post DTOs
│   │   ├── comment.py             # Comment DTOs
│   │   ├── message.py             # Message DTOs
│   │   ├── chat.py                # Chat DTOs
│   │   ├── notification.py        # Notification DTOs
│   │   ├── reaction.py            # Reaction DTOs
│   │   ├── auth.py                # Auth DTOs
│   │   └── common.py              # Common DTOs (pagination)
│   ├── services/
│   │   ├── __init__.py
│   │   ├── auth_service.py        # Auth business logic
│   │   ├── user_service.py        # User business logic
│   │   ├── post_service.py        # Post business logic
│   │   ├── comment_service.py     # Comment business logic
│   │   ├── message_service.py     # Message business logic
│   │   ├── chat_service.py        # Chat business logic
│   │   ├── notification_service.py # Notification business logic
│   │   ├── reaction_service.py    # Reaction business logic
│   │   ├── photo_service.py       # Photo business logic
│   │   ├── email_service.py       # Email service
│   │   └── websocket_service.py   # WebSocket business logic
│   ├── repositories/
│   │   ├── __init__.py
│   │   ├── base.py                # Base repository
│   │   ├── user.py                # User repository
│   │   ├── post.py                # Post repository
│   │   ├── comment.py             # Comment repository
│   │   ├── message.py             # Message repository
│   │   ├── chat.py                # Chat repository
│   │   ├── notification.py        # Notification repository
│   │   ├── reaction.py            # Reaction repository
│   │   ├── token.py               # Token repository
│   │   └── photo.py               # Photo repository
│   ├── utils/
│   │   ├── __init__.py
│   │   ├── image.py               # Image processing
│   │   ├── pagination.py          # Pagination helpers
│   │   └── helpers.py             # Utility functions
│   └── enums/
│       ├── __init__.py
│       ├── role.py                # Role enum
│       ├── status_type.py         # Status enum
│       ├── notification_type.py   # Notification enum
│       └── reaction_type.py       # Reaction enum
├── alembic/
│   ├── versions/                  # Migration scripts
│   └── env.py                     # Alembic configuration
├── tests/
│   ├── __init__.py
│   ├── conftest.py                # Pytest configuration
│   ├── unit/                      # Unit tests
│   ├── integration/               # Integration tests
│   └── e2e/                       # End-to-end tests
├── scripts/
│   ├── seed_db.py                 # Database seeding
│   └── cleanup_tokens.py          # Token cleanup
├── .env.example                   # Environment variables template
├── .pre-commit-config.yaml        # Pre-commit hooks
├── pyproject.toml                 # Project configuration
├── Dockerfile                     # Container image
├── docker-compose.yml             # Development environment
└── README.md
```

### 3. **Key Implementation Details**

#### **Database Models (SQLAlchemy 2.0 Async)**
```python
# app/models/base.py
from datetime import datetime
from sqlalchemy import DateTime, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column

class Base(DeclarativeBase):
    pass

class BaseEntity(Base):
    __abstract__ = True
    
    id: Mapped[UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), onupdate=func.now())
    version: Mapped[int] = mapped_column(default=0)
    created_by: Mapped[UUID] = mapped_column(nullable=True)
    updated_by: Mapped[UUID] = mapped_column(nullable=True)
```

#### **Pydantic Schemas with Validation**
```python
# app/schemas/user.py
from pydantic import BaseModel, EmailStr, Field, ConfigDict
from datetime import date
from typing import Optional
from uuid import UUID

class UserBase(BaseModel):
    username: str = Field(..., min_length=3, max_length=32)
    email: EmailStr
    firstname: Optional[str] = Field(None, max_length=255)
    lastname: Optional[str] = Field(None, max_length=255)
    date_of_birth: Optional[date] = None

class UserCreate(UserBase):
    password: str = Field(..., min_length=8, max_length=128)

class UserResponse(UserBase):
    id: UUID
    role: str
    enabled: bool
    created_at: datetime
    
    model_config = ConfigDict(from_attributes=True)
```

#### **JWT Authentication**
```python
# app/core/security.py
from datetime import datetime, timedelta
from typing import Optional
from jose import JWTError, jwt
from passlib.context import CryptContext

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + (expires_delta or timedelta(minutes=15))
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)
```

#### **WebSocket Implementation**
```python
# app/api/websocket/manager.py
from fastapi import WebSocket
from typing import Dict, Set
import json

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[UUID, WebSocket] = {}
        self.user_rooms: Dict[UUID, Set[UUID]] = {}  # user_id -> chat_ids
    
    async def connect(self, user_id: UUID, websocket: WebSocket)
    async def disconnect(self, user_id: UUID)
    async def send_personal_message(self, message: dict, user_id: UUID)
    async def broadcast_to_chat(self, message: dict, chat_id: UUID)

manager = ConnectionManager()
```

#### **Repository Pattern with Async**
```python
# app/repositories/base.py
from typing import Generic, TypeVar, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select

ModelType = TypeVar("ModelType")

class BaseRepository(Generic[ModelType]):
    def __init__(self, model: type[ModelType], session: AsyncSession):
        self.model = model
        self.session = session
    
    async def get(self, id: UUID) -> Optional[ModelType]:
        result = await self.session.execute(select(self.model).where(self.model.id == id))
        return result.scalar_one_or_none()
    
    async def create(self, obj: ModelType) -> ModelType:
        self.session.add(obj)
        await self.session.commit()
        await self.session.refresh(obj)
        return obj
```

#### **Dependency Injection**
```python
# app/api/deps.py
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession

security = HTTPBearer()

async def get_db() -> AsyncSession:
    async with async_session() as session:
        yield session

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: AsyncSession = Depends(get_db)
) -> User:
    token = credentials.credentials
    payload = decode_token(token)
    user = await user_repository.get_by_email(payload["sub"])
    if not user:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user
```

### 4. **Migration Strategy**

#### **Phase 1: Core Infrastructure** (2-3 weeks)
- Set up project structure
- Configure database with async SQLAlchemy
- Implement base models and repositories
- Set up Alembic migrations
- Configure Redis for caching and WebSocket state
- Implement JWT authentication
- Set up Pydantic settings

#### **Phase 2: User & Auth Module** (1-2 weeks)
- Migrate User entity and relationships
- Implement auth endpoints (register, login, logout, refresh)
- Email activation system
- Profile management
- Follow/unfollow functionality
- User search and query

#### **Phase 3: Content Module** (2-3 weeks)
- Post entity with polls
- Comment system with replies
- Reaction system
- Media upload and processing
- Saved posts functionality

#### **Phase 4: Real-time Features** (2 weeks)
- WebSocket connection manager
- Chat system (private and group)
- Message handling
- Real-time notifications
- Online status management

#### **Phase 5: Admin & Utilities** (1 week)
- Admin endpoints
- Token cleanup scheduler
- Image optimization
- Email templates

#### **Phase 6: Testing & Documentation** (1-2 weeks)
- Unit tests for services
- Integration tests for endpoints
- E2E tests for critical flows
- API documentation enhancement
- Performance testing

### 5. **2026 Best Practices Applied**

#### **Performance**
- Async/await throughout the stack
- Database connection pooling
- Redis caching for frequent queries
- Optimized queries with selectin/prefetch loading
- Background tasks for email sending

#### **Security**
- JWT with short-lived access tokens
- Token blacklist in Redis
- BCrypt password hashing
- CORS configuration
- Input validation with Pydantic
- SQL injection prevention (ORM)
- Rate limiting (slowapi)

#### **Code Quality**
- Type hints throughout (mypy strict mode)
- Pydantic v2 for runtime validation
- Repository pattern for data access
- Service layer for business logic
- Dependency injection
- Pre-commit hooks (black, ruff, mypy)
- Comprehensive test coverage

#### **Developer Experience**
- Auto-generated OpenAPI docs
- Hot reload in development
- Environment-based configuration
- Docker for local development
- Clear error messages
- Structured logging

#### **Scalability**
- Stateless authentication
- Horizontal scaling ready
- WebSocket state in Redis
- Database connection pooling
- Async operations
- Background task queue (Celery/RQ)

### 6. **Configuration Example**

```python
# app/config/settings.py
from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # Database
    DATABASE_URL: str
    
    # Redis
    REDIS_URL: str = "redis://localhost:6379"
    
    # Security
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    
    # Email
    SMTP_HOST: str
    SMTP_PORT: int
    SMTP_USER: str
    SMTP_PASSWORD: str
    
    # Frontend
    FRONTEND_URL: str
    
    # Media
    MAX_UPLOAD_SIZE: int = 3 * 1024 * 1024  # 3MB
    
    class Config:
        env_file = ".env"

settings = Settings()
```

### 7. **Key Advantages of Migration**

- **Performance**: Async operations provide better concurrency
- **Developer Velocity**: Python's simplicity vs Java verbosity
- **Modern Ecosystem**: Pydantic v2, SQLAlchemy 2.0 are cutting-edge
- **Type Safety**: Mypy + Pydantic provide excellent type checking
- **API Documentation**: FastAPI's auto-generated docs are superior
- **Testing**: Pytest's async support is more intuitive
- **Deployment**: Smaller container images, faster startup
- **Community**: Larger Python community for social media features
