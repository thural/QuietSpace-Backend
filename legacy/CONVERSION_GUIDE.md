# QuietSpace Backend: Spring Boot → FastAPI Conversion Guide

This guide provides a detailed mapping from the existing Spring Boot project structure to a FastAPI Python backend.

## Table of Contents

1. [Project Structure Mapping](#project-structure-mapping)
2. [Core Dependencies Mapping](#core-dependencies-mapping)
3. [Component-by-Component Conversion](#component-by-component-conversion)
4. [Step-by-Step Conversion Plan](#step-by-step-conversion-plan)

---

## Project Structure Mapping

| Spring Boot Component | Location | FastAPI Equivalent | Notes |
|-----------------------|----------|--------------------|-------|
| Maven `pom.xml` | `/pom.xml` | `pyproject.toml` (Poetry) | Dependency management |
| `application.yml` | `/src/main/resources/application.yml` | `.env` + `app/config.py` (pydantic-settings) | Configuration |
| Main Class | `QuietspaceApplication.java` | `app/main.py` | FastAPI app entry point |

### Detailed Directory Structure Mapping

```
Spring Boot (src/main/java/dev/thural/quietspace/)
├── authentication/
│   ├── controller/AuthController.java
│   ├── model/AuthRequest.java, RegistrationRequest.java, AuthResponse.java
│   └── service/AuthService.java
├── bootstrap/
│   ├── AdminLoader.java
│   └── TokenCleaner.java
├── config/
│   ├── AppConfig.java
│   ├── ApplicationAuditAware.java
│   ├── JpaAuditionConfiguration.java
│   ├── MailConfig.java
│   ├── OffsetDateTimeProvider.java
│   └── OpenApiConfig.java
├── controller/
│   ├── AdminController.java
│   ├── ChatController.java
│   ├── CommentController.java
│   ├── MessageController.java
│   ├── NotificationController.java
│   ├── PhotoController.java
│   ├── PostController.java
│   ├── ReactionController.java
│   └── UserController.java
├── entity/
│   ├── BaseEntity.java
│   ├── Chat.java
│   ├── Comment.java
│   ├── Message.java
│   ├── Notification.java
│   ├── Photo.java
│   ├── Poll.java
│   ├── PollOption.java
│   ├── Post.java
│   ├── ProfileSettings.java
│   ├── Reaction.java
│   ├── Token.java
│   └── User.java
├── enums/
│   ├── EmailTemplateName.java
│   ├── EntityType.java
│   ├── EventType.java
│   ├── NotificationType.java
│   ├── Permission.java
│   ├── ReactionType.java
│   ├── Role.java
│   └── StatusType.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   └── *Exception.java
├── mapper/
│   ├── ChatMapper.java
│   ├── CommentMapper.java
│   ├── MessageMapper.java
│   ├── NotificationMapper.java
│   ├── PostMapper.java
│   ├── ReactionMapper.java
│   └── UserMapper.java
├── model/
│   ├── request/*Request.java
│   └── response/*Response.java
├── query/
│   └── UserQuery.java
├── repository/
│   ├── specifications/
│   ├── ChatRepository.java
│   ├── CommentRepository.java
│   ├── MessageRepository.java
│   ├── NotificationRepository.java
│   ├── PhotoRepository.java
│   ├── PostRepository.java
│   ├── ReactionRepository.java
│   ├── TokenRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomAccessDeniedHandler.java
│   ├── JwtAuthEntryPoint.java
│   ├── JwtFilter.java
│   ├── JwtService.java
│   └── SecurityConfig.java
├── service/
│   ├── impl/*ServiceImpl.java
│   └── *Service.java
├── utils/
│   ├── ImageCompressionUtil.java
│   ├── PageUtils.java
│   └── PagingProvider.java
└── websocket/
    ├── config/
    ├── event/
    └── model/
```

```
FastAPI (app/)
├── api/
│   ├── v1/
│   │   ├── __init__.py
│   │   ├── auth.py
│   │   ├── admin.py
│   │   ├── users.py
│   │   ├── posts.py
│   │   ├── comments.py
│   │   ├── reactions.py
│   │   ├── chats.py
│   │   ├── messages.py
│   │   ├── notifications.py
│   │   └── photos.py
│   └── deps.py
├── core/
│   ├── __init__.py
│   ├── config.py
│   ├── security.py
│   └── db.py
├── models/
│   ├── __init__.py
│   ├── base.py
│   ├── user.py
│   ├── post.py
│   ├── comment.py
│   ├── reaction.py
│   ├── chat.py
│   ├── message.py
│   ├── notification.py
│   ├── photo.py
│   ├── poll.py
│   ├── poll_option.py
│   ├── profile_settings.py
│   └── token.py
├── schemas/
│   ├── __init__.py
│   ├── auth.py
│   ├── user.py
│   ├── post.py
│   ├── comment.py
│   ├── reaction.py
│   ├── chat.py
│   ├── message.py
│   ├── notification.py
│   ├── photo.py
│   ├── poll.py
│   └── common.py
├── crud/
│   ├── __init__.py
│   ├── base.py
│   ├── user.py
│   ├── post.py
│   ├── comment.py
│   ├── reaction.py
│   ├── chat.py
│   ├── message.py
│   ├── notification.py
│   ├── photo.py
│   └── token.py
├── services/
│   ├── __init__.py
│   ├── auth_service.py
│   ├── user_service.py
│   ├── post_service.py
│   ├── comment_service.py
│   ├── reaction_service.py
│   ├── chat_service.py
│   ├── message_service.py
│   ├── notification_service.py
│   ├── photo_service.py
│   └── email_service.py
├── utils/
│   ├── __init__.py
│   ├── image_utils.py
│   └── pagination_utils.py
├── websocket/
│   ├── __init__.py
│   ├── manager.py
│   ├── connection_handler.py
│   └── events.py
├── tasks/
│   ├── __init__.py
│   ├── admin_loader.py
│   └── token_cleaner.py
├── exceptions/
│   ├── __init__.py
│   └── handlers.py
├── enums/
│   ├── __init__.py
│   ├── role.py
│   ├── status_type.py
│   ├── event_type.py
│   └── ...
├── main.py
└── alembic.ini
```

---

## Core Dependencies Mapping

| Spring Boot Dependency | Purpose | FastAPI/Python Equivalent |
|-------------------------|---------|---------------------------|
| `spring-boot-starter-web` | REST API | `fastapi` + `uvicorn[standard]` |
| `spring-boot-starter-data-jpa` | ORM | `sqlalchemy` |
| `spring-boot-starter-validation` | Validation | `pydantic` |
| `spring-boot-starter-security` | Security | `python-jose[cryptography]` + `passlib[bcrypt]` |
| `spring-boot-starter-websocket` | WebSockets | Built-in FastAPI WebSockets + `redis` (for pub/sub) |
| `spring-boot-starter-mail` | Email | `fastapi-mail` |
| `spring-boot-starter-thymeleaf` | Templating | `jinja2` |
| `flyway-core` | Migrations | `alembic` |
| `mysql-connector-j` | MySQL driver | `asyncmy` (async) or `mysql-connector-python` (sync) |
| `mapstruct` | Object mapping | Manual mapping or Pydantic model conversion |
| `lombok` | Boilerplate reduction | Python dataclasses / Pydantic |
| `springdoc-openapi-starter-webmvc-ui` | API docs | FastAPI built-in docs |
| `thumbnailator` | Image processing | `Pillow` (PIL) |
| `jjwt` | JWT | `python-jose[cryptography]` |

---

## Component-by-Component Conversion

### 1. Configuration (`config/`)

**Spring Boot**: [application.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application.yml)
```yaml
spring:
  application:
    name: quietspace-backend
    security:
      jwt:
        secret-key: ${JWT_SECRET_KEY}
        expiration: 600000
```

**FastAPI Equivalent** (`app/core/config.py`):
```python
from pydantic_settings import BaseSettings
from functools import lru_cache

class Settings(BaseSettings):
    APP_NAME: str = "quietspace-backend"
    JWT_SECRET_KEY: str
    JWT_ACCESS_TOKEN_EXPIRE_MINUTES: int = 10
    JWT_REFRESH_TOKEN_EXPIRE_DAYS: int = 1
    
    DATABASE_URL: str
    FRONTEND_URL: str
    
    MAIL_HOST: str
    MAIL_PORT: int
    MAIL_USERNAME: str
    MAIL_PASSWORD: str
    
    class Config:
        env_file = ".env"

@lru_cache()
def get_settings():
    return Settings()
```

### 2. Database Entities (`entity/`)

**Spring Boot**: [BaseEntity.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/entity/BaseEntity.java)
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Version
    private Integer version;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String updatedBy;
    
    @CreatedDate
    private OffsetDateTime createDate;
    
    @LastModifiedDate
    private OffsetDateTime updateDate;
}
```

**FastAPI Equivalent** (`app/models/base.py`):
```python
from sqlalchemy import Column, String, DateTime, Integer
from sqlalchemy.dialects.mysql import CHAR
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func
import uuid
from datetime import datetime, timezone

Base = declarative_base()

def utc_now():
    return datetime.now(timezone.utc)

class BaseEntity(Base):
    __abstract__ = True
    
    id = Column(CHAR(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    version = Column(Integer, default=0)
    created_by = Column(String(255), nullable=True)
    updated_by = Column(String(255), nullable=True)
    create_date = Column(DateTime(timezone=True), default=utc_now, nullable=False)
    update_date = Column(DateTime(timezone=True), default=utc_now, onupdate=utc_now)
```

### 3. JPA Repositories → SQLAlchemy CRUD

**Spring Boot**: [UserRepository.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/repository/UserRepository.java)
```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

**FastAPI Equivalent** (`app/crud/user.py`):
```python
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from typing import Optional
from app.models.user import User
import uuid

class CRUDUser:
    async def get_by_username(self, db: AsyncSession, username: str) -> Optional[User]:
        result = await db.execute(select(User).where(User.username == username))
        return result.scalar_one_or_none()
    
    async def get_by_email(self, db: AsyncSession, email: str) -> Optional[User]:
        result = await db.execute(select(User).where(User.email == email))
        return result.scalar_one_or_none()
    
    async def exists_by_username(self, db: AsyncSession, username: str) -> bool:
        result = await db.execute(select(User.id).where(User.username == username))
        return result.scalar_one_or_none() is not None
    
    async def exists_by_email(self, db: AsyncSession, email: str) -> bool:
        result = await db.execute(select(User.id).where(User.email == email))
        return result.scalar_one_or_none() is not None

user_crud = CRUDUser()
```

### 4. Controllers → FastAPI API Endpoints

**Spring Boot**: [AuthController.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/authentication/controller/AuthController.java)
```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**FastAPI Equivalent** (`app/api/v1/auth.py`):
```python
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.db import get_db
from app.schemas.auth import RegistrationRequest, LoginRequest, AuthResponse
from app.services.auth_service import AuthService

router = APIRouter(prefix="/auth", tags=["Authentication"])

@router.post("/register", response_model=AuthResponse)
async def register(
    request: RegistrationRequest,
    db: AsyncSession = Depends(get_db)
):
    service = AuthService(db)
    return await service.register(request)

@router.post("/login", response_model=AuthResponse)
async def login(
    request: LoginRequest,
    db: AsyncSession = Depends(get_db)
):
    service = AuthService(db)
    return await service.login(request)
```

### 5. Services → FastAPI Services

**Spring Boot**: Services in [service/impl/](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/service/impl)

**FastAPI Equivalent**: Services in `app/services/` that take `AsyncSession` as a parameter

### 6. WebSockets

**Spring Boot**: [WebSocketConfig.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/websocket/config/WebSocketConfig.java) + [SocketEventListener.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/websocket/event/listener/SocketEventListener.java)

**FastAPI Equivalent** (`app/websocket/manager.py`):
```python
from fastapi import WebSocket, WebSocketDisconnect
from typing import Dict, Set
import logging

logger = logging.getLogger(__name__)

class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[str, WebSocket] = {}  # user_id: WebSocket
    
    async def connect(self, user_id: str, websocket: WebSocket):
        await websocket.accept()
        self.active_connections[user_id] = websocket
        logger.info(f"User {user_id} connected")
    
    def disconnect(self, user_id: str):
        if user_id in self.active_connections:
            del self.active_connections[user_id]
            logger.info(f"User {user_id} disconnected")
    
    async def send_personal_message(self, message: dict, user_id: str):
        if user_id in self.active_connections:
            await self.active_connections[user_id].send_json(message)
    
    async def broadcast(self, message: dict):
        for connection in self.active_connections.values():
            await connection.send_json(message)

manager = ConnectionManager()
```

### 7. Exception Handling

**Spring Boot**: [GlobalExceptionHandler.java](file:///home/thural/Github/QuietSpace-Backend/src/main/java/dev/thural/quietspace/exception/GlobalExceptionHandler.java)

**FastAPI Equivalent** (`app/exceptions/handlers.py`):
```python
from fastapi import Request, HTTPException
from fastapi.responses import JSONResponse
from sqlalchemy.exc import SQLAlchemyError

async def sqlalchemy_exception_handler(request: Request, exc: SQLAlchemyError):
    return JSONResponse(
        status_code=500,
        content={"detail": "Database error occurred"}
    )

async def http_exception_handler(request: Request, exc: HTTPException):
    return JSONResponse(
        status_code=exc.status_code,
        content={"detail": exc.detail}
    )
```

---

## Step-by-Step Conversion Plan

### Phase 1: Project Setup
1. Initialize Python project with Poetry
2. Create directory structure
3. Set up `.env` file with necessary environment variables
4. Configure `pyproject.toml` with all dependencies
5. Set up Alembic for database migrations

### Phase 2: Core Infrastructure
1. Implement database configuration and SQLAlchemy models
2. Create base CRUD class
3. Implement Pydantic schemas for requests/responses
4. Set up security utilities (JWT, password hashing)
5. Configure exception handlers

### Phase 3: Authentication & Authorization
1. Implement auth endpoints (register, login, refresh token)
2. Implement JWT dependency injection
3. Implement role-based access control dependencies
4. Port the admin loader and token cleaner tasks

### Phase 4: Core Features (One Module at a Time)
1. **User Management**: Users, profiles, followings
2. **Posts & Media**: Posts, photos, polls
3. **Interactions**: Comments, reactions
4. **Chat & Messaging**: Chats, messages
5. **Notifications**: Notifications system

### Phase 5: Real-Time Features
1. Implement WebSocket connection manager
2. Port chat and notification real-time features
3. Integrate with Redis for pub/sub (if scaling needed)

### Phase 6: Email & Utilities
1. Implement email service with Jinja2 templates
2. Port image compression utilities
3. Implement pagination utilities

### Phase 7: Testing
1. Write unit tests for services
2. Write integration tests for API endpoints
3. Write WebSocket tests

### Phase 8: Infrastructure
1. Update Dockerfile for Python/FastAPI
2. Update docker-compose.yml
3. Verify Kubernetes configurations still work

---

## Key Notes

- **Async First**: Use FastAPI with async endpoints and SQLAlchemy 2.0 async for better performance
- **Type Hints**: Always use type hints (required by FastAPI and Pydantic)
- **Dependency Injection**: Leverage FastAPI's dependency system for database sessions, authentication, etc.
- **Database Migrations**: You can reuse existing Flyway SQL scripts in Alembic
