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
fastapi==0.115.4             # Modern async framework
uvicorn[standard]==0.34.0    # ASGI server with http2 support
pydantic==2.10.2             # v2 for better validation
pydantic-settings==2.7.1     # Settings management

# Database & ORM (Syllabus-aligned)
sqlmodel==0.0.23             # SQLAlchemy wrapper with Pydantic integration
asyncpg==0.30.0              # Async PostgreSQL driver (syllabus requirement)
alembic==1.14.2             # Database migrations

# Authentication & Security
passlib[bcrypt]==1.7.4      # Password hashing
python-jose[cryptography]==3.3.0  # JWT handling
python-multipart==0.0.20     # Form data handling
itsdangerous==2.2.0         # Token generation
slowapi==0.1.12             # Rate limiting

# Background Tasks & Caching (Syllabus-aligned)
celery==5.5.0               # Distributed task queue
redis==5.2.1                # Redis for Celery broker, caching, and WebSocket state
structlog==25.2.0            # Structured logging (syllabus requirement)

# WebSocket & Real-time (Full Functionality)
websockets==14.1            # WebSocket support
python-socketio==5.13.0     # Socket.IO for real-time messaging (replaces STOMP)
aiohttp==3.11.0             # Async HTTP client for WebSocket connections

# Email (Full Functionality)
fastapi-mail==1.5.0         # Async email handling
jinja2==3.2.0               # Email templates

# Image Processing
pillow==11.0.0              # Image processing
python-magic==0.4.29        # File type detection

# API Documentation
fastapi-swagger-ui==0.0.2   # Enhanced Swagger UI

# Testing (Syllabus-aligned)
pytest==8.4.2               # Modern testing
pytest-asyncio==0.25.0      # Async test support
httpx==0.28.1               # Async HTTP client for testing
pytest-cov==6.1.0           # Coverage

# Development & Quality (Syllabus-aligned)
poetry-core==1.9.1           # Poetry for dependency management (syllabus requirement)
black==25.1.0               # Code formatting
ruff==0.8.4                 # Fast linter (syllabus requirement)
mypy==1.13.0                # Type checking
pre-commit==4.0.1           # Git hooks
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
│   │       ├── handlers.py        # WebSocket message handlers
│   │       └── socketio.py        # Socket.IO configuration (replaces STOMP)
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
│   │   ├── email_service.py       # Email service (activation, notifications)
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
├── templates/
│   └── email/                     # Email templates (Jinja2)
│       ├── activation.html
│       └── notification.html
├── .env.example                   # Environment variables template
├── .pre-commit-config.yaml        # Pre-commit hooks
├── pyproject.toml                 # Poetry project configuration (syllabus requirement)
├── Dockerfile                     # Multi-stage container image with non-privileged user
├── docker-compose.yml             # Development environment with PostgreSQL
├── .github/
│   └── workflows/
│       └── ci-cd.yml              # GitHub Actions CI workflow (syllabus requirement)
└── README.md
```

### 3. **Key Implementation Details**

#### **Database Models (SQLModel with Async PostgreSQL)**
```python
# app/models/base.py
from datetime import datetime
from sqlmodel import SQLModel, Field
from uuid import UUID, uuid4
from typing import Optional

class BaseEntity(SQLModel):
    id: Optional[UUID] = Field(default_factory=uuid4, primary_key=True)
    created_at: datetime = Field(default_factory=datetime.utcnow)
    updated_at: Optional[datetime] = Field(default_factory=datetime.utcnow)
    version: int = Field(default=0)
    created_by: Optional[UUID] = Field(default=None)
    updated_by: Optional[UUID] = Field(default=None)
```

#### **Pydantic v2 Validators (Syllabus-aligned)**
```python
# app/schemas/post.py
from pydantic import BaseModel, Field, field_validator, model_validator
from typing import Optional

class PostCreate(BaseModel):
    text: str = Field(..., min_length=1, max_length=280)
    title: Optional[str] = Field(None, max_length=255)
    
    @field_validator('text')
    @classmethod
    def sanitize_text(cls, v: str) -> str:
        # Profanity filter and banned words check
        banned_words = ['spam', 'abuse']
        if any(word in v.lower() for word in banned_words):
            raise ValueError('Text contains prohibited content')
        return v.strip()
    
    @model_validator(mode='after')
    def validate_content_length(self) -> 'PostCreate':
        if self.text and len(self.text) > 280:
            raise ValueError('Post exceeds 280 character limit')
        return self
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

#### **WebSocket Implementation (Socket.IO - Full Functionality)**
```python
# app/api/websocket/socketio.py
from fastapi import FastAPI
from socketio import AsyncServer, ASGIApp
from app.config.settings import settings
import redis.asyncio as redis

# Socket.IO configuration (replaces STOMP)
socketio = AsyncServer(
    async_mode='asgi',
    cors_allowed_origins=[settings.FRONTEND_URL],
    logger=True,
    engineio_logger=True
)

# Redis adapter for horizontal scaling
redis_client = redis.from_url(settings.REDIS_URL)
socketio_manager = socketio.AsyncRedisManager(settings.REDIS_URL)
socketio = AsyncServer(
    async_mode='asgi',
    cors_allowed_origins=[settings.FRONTEND_URL],
    client_manager=socketio_manager
)

# app/api/websocket/manager.py
from typing import Dict, Set
from uuid import UUID
from app.api.websocket.socketio import socketio

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[UUID, str] = {}  # user_id -> session_id
        self.user_rooms: Dict[UUID, Set[UUID]] = {}  # user_id -> chat_ids
    
    async def connect_user(self, user_id: UUID, session_id: str):
        self.active_connections[user_id] = session_id
        await socketio.emit('user_connected', {'user_id': str(user_id)})
    
    async def disconnect_user(self, user_id: UUID):
        if user_id in self.active_connections:
            del self.active_connections[user_id]
            await socketio.emit('user_disconnected', {'user_id': str(user_id)})
    
    async def send_to_user(self, user_id: UUID, event: str, data: dict):
        if user_id in self.active_connections:
            session_id = self.active_connections[user_id]
            await socketio.emit(event, data, room=session_id)
    
    async def broadcast_to_chat(self, chat_id: UUID, event: str, data: dict):
        await socketio.emit(event, data, room=f'chat_{chat_id}')
    
    async def join_chat_room(self, user_id: UUID, chat_id: UUID):
        session_id = self.active_connections.get(user_id)
        if session_id:
            await socketio.enter_room(session_id, f'chat_{chat_id}')
            if user_id not in self.user_rooms:
                self.user_rooms[user_id] = set()
            self.user_rooms[user_id].add(chat_id)

manager = ConnectionManager()

# app/api/websocket/handlers.py
from app.api.websocket.socketio import socketio
from app.api.websocket.manager import manager
from app.services.message_service import message_service

@socketio.on('connect')
async def handle_connect(sid, environ):
    # Extract JWT from handshake
    auth_token = environ.get('HTTP_AUTHORIZATION', '').replace('Bearer ', '')
    user = await authenticate_websocket_token(auth_token)
    if user:
        await manager.connect_user(user.id, sid)
        await socketio.emit('connected', {'user_id': str(user.id)}, to=sid)

@socketio.on('disconnect')
async def handle_disconnect(sid):
    # Find and disconnect user
    for user_id, session_id in list(manager.active_connections.items()):
        if session_id == sid:
            await manager.disconnect_user(user_id)
            break

@socketio.on('join_chat')
async def handle_join_chat(sid, data):
    user_id = UUID(data['user_id'])
    chat_id = UUID(data['chat_id'])
    await manager.join_chat_room(user_id, chat_id)

@socketio.on('send_message')
async def handle_send_message(sid, data):
    message_data = {
        'chat_id': UUID(data['chat_id']),
        'sender_id': UUID(data['sender_id']),
        'recipient_id': UUID(data['recipient_id']),
        'text': data['text']
    }
    saved_message = await message_service.add_message(message_data)
    
    # Send to both sender and recipient
    await manager.send_to_user(saved_message.sender_id, 'new_message', saved_message.dict())
    await manager.send_to_user(saved_message.recipient_id, 'new_message', saved_message.dict())
    
    # Broadcast to chat room
    await manager.broadcast_to_chat(saved_message.chat_id, 'message_in_chat', saved_message.dict())

@socketio.on('set_online_status')
async def handle_online_status(sid, data):
    user_id = UUID(data['user_id'])
    status = data['status']  # 'online' or 'offline'
    await manager.broadcast_to_chat(user_id, 'user_status', {'user_id': str(user_id), 'status': status})

# app/main.py (integrate Socket.IO)
from app.api.websocket.socketio import socketio

app = FastAPI(lifespan=lifespan)
socketio_app = ASGIApp(socketio, app)
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

#### **FastAPI Lifespan Handler, Health Endpoint & Rate Limiting (Syllabus-aligned)**
```python
# app/main.py
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from sqlmodel import SQLModel, create_engine, AsyncSession
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker
from structlog import get_logger
import structlog
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

logger = get_logger()
limiter = Limiter(key_func=get_remote_address, storage_uri=settings.REDIS_URL)

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup
    logger.info("Starting application...")
    # Initialize database connection pool
    engine = create_async_engine(
        settings.DATABASE_URL,
        pool_size=10,
        max_overflow=20,
        pool_pre_ping=True
    )
    async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    app.state.engine = engine
    app.state.async_session = async_session
    yield
    # Shutdown
    logger.info("Shutting down application...")
    await engine.dispose()

app = FastAPI(lifespan=lifespan)
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# Health endpoint
@app.get("/health", tags=["Health"])
@limiter.limit("100/minute")
async def health_check(request: Request):
    """Health check endpoint for monitoring and Docker healthchecks"""
    # Simple database ping check
    try:
        async with app.state.async_session() as session:
            from sqlalchemy import text
            await session.execute(text("SELECT 1"))
            db_status = "healthy"
    except Exception:
        db_status = "unhealthy"
    
    return {
        "status": "healthy" if db_status == "healthy" else "unhealthy",
        "database": db_status,
        "version": "1.0.0"
    }
```

#### **Annotated Dependency Injection (Syllabus-aligned)**
```python
# app/api/deps.py
from typing import Annotated
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession
from app.main import app

# Reusable type aliases
AsyncSessionDep = Annotated[AsyncSession, Depends(get_db)]
CurrentUserDep = Annotated[User, Depends(get_current_user)]

async def get_db() -> AsyncSession:
    async with app.state.async_session() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close()

async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(HTTPBearer())
) -> User:
    token = credentials.credentials
    payload = decode_token(token)
    user = await user_repository.get_by_email(payload["sub"])
    if not user:
        raise HTTPException(status_code=401, detail="Invalid token")
    return user
```

#### **Connection Pooling & Relationship Loading (Syllabus-aligned)**
```python
# app/config/database.py
from sqlmodel import create_engine
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import selectinload, joinedload

# Connection pool configuration
engine = create_async_engine(
    settings.DATABASE_URL,
    pool_size=10,              # Base connection pool size
    max_overflow=20,           # Additional connections when pool is full
    pool_pre_ping=True,         # Verify connections before use
    pool_recycle=3600,          # Recycle connections after 1 hour
    echo=settings.DEBUG
)

# Relationship loading strategy example
async def get_user_with_posts(user_id: UUID) -> User:
    async with async_session() as session:
        result = await session.execute(
            select(User)
            .options(
                selectinload(User.posts),           # Separate query for posts
                selectinload(User.followings),     # Separate query for followings
                joinedload(User.profile_settings)  # JOIN for single related object
            )
            .where(User.id == user_id)
        )
        return result.scalar_one_or_none()
```

#### **Global Exception Handler (Syllabus-aligned)**
```python
# app/core/exceptions.py
from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from pydantic import ValidationError
from sqlmodel import SQLModel

class ErrorResponse(SQLModel):
    detail: str
    error_code: str
    field: str | None = None

def register_exception_handlers(app: FastAPI):
    @app.exception_handler(ValidationError)
    async def validation_exception_handler(request: Request, exc: ValidationError):
        return JSONResponse(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            content=ErrorResponse(
                detail="Validation failed",
                error_code="VALIDATION_ERROR",
                field=str(exc.errors()[0].get("loc", ["body"])[-1])
            ).model_dump()
        )
    
    @app.exception_handler(HTTPException)
    async def http_exception_handler(request: Request, exc: HTTPException):
        return JSONResponse(
            status_code=exc.status_code,
            content=ErrorResponse(
                detail=exc.detail,
                error_code=f"HTTP_{exc.status_code}"
            ).model_dump()
        )
    
    @app.exception_handler(Exception)
    async def general_exception_handler(request: Request, exc: Exception):
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content=ErrorResponse(
                detail="Internal server error",
                error_code="INTERNAL_ERROR"
            ).model_dump()
        )
```

### 4. **Migration Strategy**

#### **Email Service Implementation (Full Functionality)**
```python
# app/services/email_service.py
from fastapi_mail import FastMail, MessageSchema, MessageType
from fastapi_mail.config import ConnectionConfig
from jinja2 import Template
from app.config.settings import settings
from app.enums import EmailTemplateName

# Email configuration
conf = ConnectionConfig(
    MAIL_USERNAME=settings.SMTP_USER,
    MAIL_PASSWORD=settings.SMTP_PASSWORD,
    MAIL_FROM=settings.SMTP_USER,
    MAIL_PORT=settings.SMTP_PORT,
    MAIL_SERVER=settings.SMTP_HOST,
    MAIL_STARTTLS=True,
    MAIL_SSL_TLS=False,
    USE_CREDENTIALS=True,
    VALIDATE_CERTS=True
)

fastmail = FastMail(conf)

class EmailService:
    async def send_email(
        self,
        email_to: str,
        subject: str,
        template_name: EmailTemplateName,
        context: dict
    ):
        """Send email using Jinja2 template"""
        # Load and render template
        template_path = f"templates/email/{template_name.value}.html"
        with open(template_path) as f:
            template = Template(f.read())
        
        html_body = template.render(**context)
        
        message = MessageSchema(
            subject=subject,
            recipients=[email_to],
            body=html_body,
            subtype=MessageType.html
        )
        
        await fastmail.send_message(message)
    
    async def send_activation_email(
        self,
        user_email: str,
        user_name: str,
        activation_code: str
    ):
        """Send account activation email"""
        await self.send_email(
            email_to=user_email,
            subject="Activate Your QuietSpace Account",
            template_name=EmailTemplateName.ACTIVATE_ACCOUNT,
            context={
                "username": user_name,
                "activation_code": activation_code,
                "activation_url": f"{settings.FRONTEND_URL}/activate?code={activation_code}"
            }
        )
    
    async def send_notification_email(
        self,
        user_email: str,
        user_name: str,
        notification_type: str,
        content: str
    ):
        """Send notification email"""
        await self.send_email(
            email_to=user_email,
            subject=f"New Notification: {notification_type}",
            template_name=EmailTemplateName.NOTIFICATION,
            context={
                "username": user_name,
                "notification_type": notification_type,
                "content": content
            }
        )

# templates/email/activation.html
<!DOCTYPE html>
<html>
<head>
    <title>Activate Your Account</title>
</head>
<body>
    <h1>Welcome to QuietSpace, {{ username }}!</h1>
    <p>Your activation code is: <strong>{{ activation_code }}</strong></p>
    <p>Or click <a href="{{ activation_url }}">here</a> to activate your account.</p>
    <p>This code will expire in 15 minutes.</p>
</body>
</html>

# templates/email/notification.html
<!DOCTYPE html>
<html>
<head>
    <title>New Notification</title>
</head>
<body>
    <h1>Hello, {{ username }}!</h1>
    <p>You have a new {{ notification_type }}.</p>
    <p>{{ content }}</p>
    <p>Visit QuietSpace to see more details.</p>
</body>
</html>

# app/enums/email_template_name.py
from enum import Enum

class EmailTemplateName(str, Enum):
    ACTIVATE_ACCOUNT = "activation"
    NOTIFICATION = "notification"
```

#### **Celery Background Tasks (Syllabus-aligned)**
```python
# app/celery_app.py
from celery import Celery
from app.config.settings import settings

celery_app = Celery(
    "quietspace",
    broker=settings.REDIS_URL,
    backend=settings.REDIS_URL
)

celery_app.conf.update(
    task_serializer="json",
    accept_content=["json"],
    result_serializer="json",
    timezone="UTC",
    enable_utc=True,
)

# app/tasks/image_tasks.py
from app.celery_app import celery_app
from PIL import Image
import io

@celery_app.task
def process_image_upload(image_data: bytes, user_id: str) -> dict:
    """Background task for image compression and optimization"""
    try:
        img = Image.open(io.BytesIO(image_data))
        # Resize and compress
        img.thumbnail((800, 800))
        output = io.BytesIO()
        img.save(output, format="JPEG", quality=85, optimize=True)
        return {"status": "success", "user_id": user_id}
    except Exception as e:
        return {"status": "error", "message": str(e)}

# app/tasks/notification_tasks.py
@celery_app.task
def send_bulk_notifications(user_ids: list[str], message: str) -> dict:
    """Background task for sending notifications to multiple users"""
    # Process notifications without blocking the web server
    return {"processed": len(user_ids)}
```

#### **Transactional Testing Infrastructure (Syllabus-aligned)**
```python
# tests/conftest.py
import pytest
from httpx import AsyncClient
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from sqlmodel import SQLModel
from app.main import app
from app.models.base import BaseEntity

# Test database URL
TEST_DATABASE_URL = "postgresql+asyncpg://test:test@localhost:5433/test_db"

@pytest.fixture(scope="function")
async def db_session():
    """Create a clean database session for each test"""
    engine = create_async_engine(TEST_DATABASE_URL, echo=False)
    async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    
    async with async_session() as session:
        async with engine.begin() as conn:
            await conn.run_sync(SQLModel.metadata.create_all)
        
        yield session
        
        # Rollback after each test
        await session.rollback()
        await engine.dispose()

@pytest.fixture(scope="function")
async def client(db_session: AsyncSession):
    """Create test client with dependency overrides"""
    from app.api.deps import get_db
    
    async def override_get_db():
        yield db_session
    
    app.dependency_overrides[get_db] = override_get_db
    
    async with AsyncClient(app=app, base_url="http://test") as test_client:
        yield test_client
    
    app.dependency_overrides.clear()

# tests/integration/test_posts.py
@pytest.mark.asyncio
async def test_create_post_validation(client: AsyncClient):
    """Test that post validation returns 422 for invalid content"""
    response = await client.post(
        "/api/v1/posts",
        json={"text": "a" * 281}  # Exceeds 280 character limit
    )
    assert response.status_code == 422
    assert "VALIDATION_ERROR" in response.json()["error_code"]
```

#### **Multi-stage Dockerfile (Syllabus-aligned)**
```dockerfile
# Dockerfile
FROM python:3.12-slim as builder

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    gcc \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# Install Poetry
COPY pyproject.toml poetry.lock ./
RUN pip install poetry && poetry config virtualenvs.create false

# Install dependencies
RUN poetry install --only=main --no-interaction

FROM python:3.12-slim as runtime

# Create non-privileged user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy dependencies from builder
COPY --from=builder /usr/local/lib/python3.12/site-packages /usr/local/lib/python3.12/site-packages
COPY --from=builder /usr/local/bin /usr/local/bin

# Copy application code
COPY --chown=appuser:appuser ./app ./app

# Switch to non-privileged user
USER appuser

# Expose port
EXPOSE 8000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD python -c "import httpx; httpx.get('http://localhost:8000/health')"

# Run application
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

#### **GitHub Actions CI Workflow (Syllabus-aligned)**
```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
          POSTGRES_DB: test_db
        ports:
          - 5433:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.12'
      
      - name: Install Poetry
        run: pip install poetry
      
      - name: Install dependencies
        run: poetry install --with dev
      
      - name: Code style check with Ruff
        run: poetry run ruff check .
      
      - name: Type check with MyPy
        run: poetry run mypy app/
      
      - name: Run tests with pytest
        run: poetry run pytest tests/ -v --cov=app --cov-report=xml
        env:
          DATABASE_URL: postgresql+asyncpg://test:test@localhost:5433/test_db
      
      - name: Build Docker image
        run: docker build -t quietspace-backend:${{ github.sha }} .
      
      - name: Run container smoke test
        run: |
          docker run -d -p 8000:8000 --name test-container quietspace-backend:${{ github.sha }}
          sleep 10
          curl -f http://localhost:8000/health || exit 1
          docker stop test-container
```

### 4. **Migration Strategy (Full Functionality + Syllabus-aligned)**

#### **Phase 1: Core Infrastructure** (Weeks 1-4)
- Initialize Poetry project with pyproject.toml
- Set up FastAPI app with lifespan handler (asynccontextmanager)
- Configure PostgreSQL with asyncpg driver
- Implement connection pooling (pool_size, max_overflow, pool_pre_ping)
- Create SQLModel base entity with audit fields
- Set up global exception handler (errors.py)
- Configure structlog for structured logging
- Implement Annotated dependency injection aliases
- Set up CORS and middleware interceptors
- Configure Redis for caching and WebSocket state

#### **Phase 2: User & Auth Module** (Weeks 5-6)
- Migrate User entity to SQLModel with asyncpg
- Implement Pydantic v2 validators (@field_validator, @model_validator)
- Configure Alembic for async migrations
- Implement auth endpoints with JWT
- Add profanity/banned-words filter in validators
- User search and query with relationship loading strategies
- **Email activation system with Jinja2 templates**
- **Email service with fastapi-mail**

#### **Phase 3: Content Module** (Weeks 7-8)
- Post entity with polls using SQLModel
- Comment system with selectinload/joinedload strategies
- Reaction system
- Set up Celery for image compression background tasks
- Saved posts functionality

#### **Phase 4: Real-time Communication** (Weeks 9-10)
- **Socket.IO configuration (replaces STOMP)**
- **WebSocket connection manager with Redis state**
- **Chat system (private and group)**
- **Message handling with delivery/read receipts**
- **Online status management**
- **Real-time notifications via WebSocket**

#### **Phase 5: Testing & CI/CD** (Weeks 11-12)
- Implement transactional testing with db_session fixture
- Add integration tests with dependency_overrides
- Create multi-stage Dockerfile with non-privileged user
- Set up GitHub Actions CI workflow
- Implement Redis caching layer
- Performance testing and optimization
- **WebSocket integration testing**

### 5. **2026 Best Practices Applied**

#### **Performance (Syllabus-aligned)**
- Async/await throughout the stack with asyncio event loop
- Database connection pooling (pool_size, max_overflow, pool_pre_ping)
- Redis caching for high-traffic social media data
- Optimized queries with selectinload/joinedload strategies
- Celery background tasks for heavy operations (image compression, notifications)
- ProcessPoolExecutor for CPU-bound operations

#### **Security**
- JWT with short-lived access tokens
- Token blacklist in Redis
- BCrypt password hashing
- CORS configuration
- Input validation with Pydantic
- SQL injection prevention (ORM)
- Rate limiting with slowapi (Redis-backed)

#### **Code Quality (Syllabus-aligned)**
- Type hints throughout (mypy strict mode)
- Pydantic v2 with @field_validator and @model_validator for input sanitization
- Repository pattern for data access
- Service layer for business logic
- Annotated dependency injection aliases
- Pre-commit hooks (black, ruff, mypy)
- Ruff for fast linting (syllabus requirement)
- Comprehensive test coverage with pytest-asyncio

#### **Developer Experience (Syllabus-aligned)**
- Auto-generated OpenAPI docs from Python class configurations
- Hot reload in development
- Environment-based configuration with Pydantic Settings
- Docker for local development
- Structured logging with structlog (syllabus requirement)
- Global exception handler eliminating try/except in routers

#### **Scalability**
- Stateless authentication
- Horizontal scaling ready
- WebSocket state in Redis
- Database connection pooling
- Async operations
- Background task queue (Celery/RQ)

### 6. **Configuration Example (Full Functionality + Syllabus-aligned)**

```python
# app/config/settings.py
from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # Database (PostgreSQL with asyncpg)
    DATABASE_URL: str = "postgresql+asyncpg://user:pass@localhost:5432/quietspace"
    
    # Redis (for Celery broker, caching, and WebSocket state)
    REDIS_URL: str = "redis://localhost:6379"
    
    # Security
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 10
    REFRESH_TOKEN_EXPIRE_DAYS: int = 7
    
    # Email (Full Functionality)
    SMTP_HOST: str
    SMTP_PORT: int
    SMTP_USER: str
    SMTP_PASSWORD: str
    
    # Frontend
    FRONTEND_URL: str
    
    # Media
    MAX_UPLOAD_SIZE: int = 3 * 1024 * 1024  # 3MB
    
    # Debug
    DEBUG: bool = False
    
    class Config:
        env_file = ".env"

settings = Settings()
```

```dotenv
# .env.example
# Database
DATABASE_URL=postgresql+asyncpg://user:password@localhost:5432/quietspace

# Redis
REDIS_URL=redis://localhost:6379/0

# Security
SECRET_KEY=your-super-secret-key-change-this-in-production
ALGORITHM=HS256
ACCESS_TOKEN_EXPIRE_MINUTES=10
REFRESH_TOKEN_EXPIRE_DAYS=7

# Email
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USER=noreply@example.com
SMTP_PASSWORD=your-smtp-password

# Frontend
FRONTEND_URL=http://localhost:3000

# Media
MAX_UPLOAD_SIZE=3145728

# Debug
DEBUG=True
```

### 7. **Syllabus Alignment Summary**

**Fully Aligned Components:**
- FastAPI with async/await and lifespan handlers
- PostgreSQL with asyncpg driver (replaced MySQL)
- SQLModel for ORM (replaced pure SQLAlchemy)
- Alembic for migrations
- Pydantic v2 with validators (@field_validator, @model_validator)
- Annotated dependency injection
- Global exception handler
- Connection pooling configuration
- Relationship loading strategies (selectinload/joinedload)
- Celery for background tasks
- Redis for caching, Celery broker, WebSocket state, and rate limiting
- structlog for structured logging
- Poetry for dependency management
- Ruff for linting
- slowapi for rate limiting
- Transactional testing with pytest-asyncio
- HTTPX for async HTTP testing
- Multi-stage Dockerfile with non-privileged user
- GitHub Actions CI workflow
- Healthcheck endpoint

**Additional Features (Beyond Syllabus - Required for Full Functionality):**
- **Socket.IO for real-time WebSocket messaging** (replaces STOMP)
- **WebSocket connection manager with Redis state**
- **Email service with fastapi-mail**
- **Jinja2 templates for email** (used only for email, not SSR)
- **Chat system (private and group)**
- **Message delivery/read receipts**
- **Online status management**
- **Real-time notifications via WebSocket**

**Syllabus Exclusions (Still Honored):**
- Built-in FastAPI OAuth2 (JWT-only approach)
- Synchronous SQLAlchemy/SQLite (async-only)
- Server-side rendering (Jinja2 only used for email templates)
- End-to-end tests (syllabus focuses on integration tests)

**Key Syllabus Concepts Covered:**
- Single-threaded async concurrency with asyncio
- Application lifecycles with asynccontextmanager
- Deterministic type safety with Pydantic v2
- Enterprise architecture with APIRouters
- Async database engineering with connection pooling
- Relationship loading strategies to prevent greenlet errors
- Global exception interception
- Distributed background worker pools with Celery
- Transactional testing infrastructure
- Production packaging with Docker
- CI/CD with GitHub Actions

### 8. **Key Advantages of Migration**

- **Performance**: Async operations provide better concurrency
- **Developer Velocity**: Python's simplicity vs Java verbosity
- **Modern Ecosystem**: Pydantic v2, SQLModel, asyncpg are cutting-edge
- **Type Safety**: Mypy + Pydantic provide excellent type checking
- **API Documentation**: FastAPI's auto-generated docs are superior
- **Testing**: Pytest's async support is more intuitive
- **Deployment**: Smaller container images, faster startup
- **Community**: Larger Python community for social media features
