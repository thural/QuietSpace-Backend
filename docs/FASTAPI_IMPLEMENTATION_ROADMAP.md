# FastAPI Implementation Roadmap

This document serves as the actionable, step-by-step strategic implementation plan for converting the QuietSpace Spring Boot backend to a modern FastAPI application. It strictly follows the architecture and capabilities outlined in `FASTAPI_MIGRATION_PLAN.md` and represents 100% of the planned functionality.

---

## Phase 1: Foundational Phase (Highest Priority)
**Goal:** Establish the project skeleton, dependency management, and core server configuration.

### 1.1. Initialize Project & Dependency Management
- **Task 1:** Initialize the project using Poetry (`poetry init`).
- **Task 2:** Install core dependencies: `fastapi`, `uvicorn[standard]`, `pydantic`, `pydantic-settings`, `python-multipart` (for form data), and `itsdangerous` (for token generation).
- **Task 3:** Install development tooling: `ruff` (linting), `black` (formatting), `mypy` (typing), `pre-commit`.
- **Task 4:** Install enhanced API documentation tools: `fastapi-swagger-ui`.

### 1.2. Setup Project Structure
- **Task 1:** Create the standard enterprise directory layout: 
  - `app/api/v1` (with `router.py`), `app/api/websocket`, `app/api/deps.py`
  - `app/core`, `app/models`, `app/schemas`, `app/services`, `app/repositories`, `app/utils`, `app/enums`, `app/tasks`
  - `alembic/`, `tests/`, `scripts/`, `templates/email/`
- **Task 2:** Initialize `app/main.py` as the application entry point.

### 1.3. Environment Configuration & Secret Management
- **Task 1:** Create `app/config/settings.py` extending `pydantic_settings.BaseSettings` for type-safe environment variables.
- **Task 2:** Define `.env.example` with required keys (DATABASE_URL, REDIS_URL, SECRET_KEY, SMTP settings).

### 1.4. Base Server & Lifespan Setup
- **Task 1:** Implement the `asynccontextmanager` lifespan handler in `main.py` for database/redis startup/shutdown events.
- **Task 2:** Implement the `/health` endpoint for Docker and orchestration health checks.

---

## Phase 2: Core Infrastructure Phase
**Goal:** Implement cross-cutting systems, database connectivity, and security middleware.

### 2.1. Database Connectivity & ORM Configuration
- **Task 1:** Install `sqlmodel`, `asyncpg`, and `alembic`.
- **Task 2:** Configure the async SQLAlchemy engine with connection pooling (`pool_size=10`, `max_overflow=20`, `pool_pre_ping=True`).
- **Task 3:** Initialize Alembic (`alembic init async`) and configure `env.py` to support SQLModel metadata.
- **Task 4:** Create `app/models/base.py` with `BaseEntity` (UUID primary keys, audit timestamps `created_at`/`updated_at`, and audit fields `created_by`/`updated_by`).
- **Task 5:** Define relationship loading strategies (`selectinload`, `joinedload`) in repository abstractions to prevent N+1 queries.

### 2.2. Authentication, Authorization & Audit
- **Task 1:** Install `python-jose[cryptography]` and `passlib[bcrypt]`.
- **Task 2:** Implement JWT token generation, validation utilities, and Redis token blacklisting in `app/core/security.py`.
- **Task 3:** Create FastAPI dependencies (`Depends(get_current_user)`) using `HTTPBearer` to protect routes.
- **Task 4:** Implement `app/core/audit.py` to automatically populate `created_by`/`updated_by` fields using the current authenticated user context.

### 2.3. Cross-Cutting Concerns
- **Task 1:** Integrate `structlog` for structured JSON logging across requests.
- **Task 2:** Install and configure `slowapi` with Redis storage to replicate rate-limiting requirements.
- **Task 3:** Create utility scripts: `scripts/seed_db.py` (for test data) and `scripts/cleanup_tokens.py` (for database/redis maintenance).

### 2.4. Error Handling & CORS
- **Task 1:** Implement global exception handlers in `app/core/exceptions.py` for `ValidationError`, `HTTPException`, and generic exceptions, returning standardized JSON formats.
- **Task 2:** Configure FastAPI `CORSMiddleware` using `settings.FRONTEND_URL`.

---

## Phase 3: Feature Replication Phase
**Goal:** Replicate all business logic and endpoints, ordered by business criticality.

### 3.1. User Management, Auth & Admin (Critical Foundation)
- **Task 1:** Create Enums (`role.py`, `status_type.py`) in `app/enums`.
- **Task 2:** Create User entity, ProfileSettings, and corresponding Pydantic schemas with validators (e.g., profanity filters).
- **Task 3:** Implement `UserService` and `AuthService`.
- **Task 4:** Implement Endpoints: `/api/v1/auth` (register, login, refresh, signout).
- **Task 5:** Implement Endpoints: `/api/v1/users` (profile, follow/unfollow, block, search).
- **Task 6:** Implement Endpoints: `/api/v1/admin` (user management, requiring `ADMIN` role).
- **Task 7:** Implement `EmailService` using `fastapi-mail` and `Jinja2` templates for account activation.

### 3.2. Core Content & Media (High Impact)
- **Task 1:** Install `Pillow` and `python-magic` (for secure file type detection).
- **Task 2:** Create Post, Poll, and Photo entities and schemas.
- **Task 3:** Implement `PostService` (including "Saved Posts" logic) and `PhotoService`.
- **Task 4:** Integrate `Celery` with Redis broker. Implement background tasks for image compression. Implement `ProcessPoolExecutor` for CPU-bound image manipulation fallbacks if needed.
- **Task 5:** Implement Endpoints: `/api/v1/posts` (CRUD, pagination, search, save/unsave), `/api/v1/photos/profile`.

### 3.3. Social Interactions (Medium Impact)
- **Task 1:** Create Comment and Reaction entities and schemas.
- **Task 2:** Implement `CommentService` and `ReactionService`.
- **Task 3:** Implement Endpoints: `/api/v1/comments` (CRUD, nested replies), `/api/v1/reactions` (toggle, counts).

### 3.4. Real-Time Communications (High Complexity)
- **Task 1:** Install `python-socketio` and `aiohttp`.
- **Task 2:** Create Chat and Message entities and schemas.
- **Task 3:** Configure `AsyncRedisManager` to scale Socket.IO across multiple workers.
- **Task 4:** Implement `app/api/websocket/manager.py` for user connection state and `app/api/websocket/handlers.py` for events (`join_chat`, `send_message`, `set_online_status`). Ensure WebSocket handlers explicitly support delivery and read receipts for messages.
- **Task 5:** Implement REST fallbacks: `/api/v1/chats` and `/api/v1/messages`.

### 3.5. Notification System (Medium Complexity)
- **Task 1:** Create Notification entity and `notification_type.py` enum.
- **Task 2:** Implement `NotificationService`.
- **Task 3:** Implement in-app Socket.IO broadcasts for real-time notifications (likes, follows, messages).
- **Task 4:** Implement Celery background tasks to send email notifications for offline users.
- **Task 5:** Implement Endpoints: `/api/v1/notifications`.

---

## Phase 4: Validation and Completion Phase
**Goal:** Ensure 1:1 parity, security, and production readiness.

### 4.1. End-to-End & Transactional Testing
- **Task 1:** Install `pytest`, `pytest-asyncio`, `httpx`, and `pytest-cov`.
- **Task 2:** Configure `conftest.py` with a transactional `db_session` fixture (rolling back after each test).
- **Task 3:** Write integration tests for all API endpoints to verify functional parity with Spring Boot.
- **Task 4:** Write WebSocket integration tests using Socket.IO test clients.

### 4.2. Performance Benchmarking
- **Task 1:** Conduct load testing on critical endpoints (Auth, Posts) to ensure asyncpg and connection pooling meet or exceed current JVM performance.
- **Task 2:** Validate Redis caching hit-rates and Socket.IO horizontal scaling limits.

### 4.3. Security Scanning
- **Task 1:** Run `bandit` and `safety` to scan for known vulnerabilities in Python dependencies.
- **Task 2:** Verify JWT expiration, Redis token blacklisting logic, and `slowapi` rate-limiting protections via automated tests.

### 4.4. Deployment Readiness Checks
- **Task 1:** Finalize the multi-stage Dockerfile (ensuring the `appuser` non-privileged user is configured correctly and healthchecks pass).
- **Task 2:** Test the complete stack using `docker-compose.yml` (FastAPI, PostgreSQL, Redis, Celery worker).
- **Task 3:** Validate GitHub Actions CI/CD workflow passes all formatting (`ruff`), typing (`mypy`), and test (`pytest`) gates.