# QuietSpace Backend

## Project Overview

QuietSpace is a privacy-focused social media application designed for meaningful interactions. This repository contains the backend API, built with FastAPI and Python, providing a comprehensive set of features for modern social networking with an emphasis on clean code, security, and real-time interaction.

## Features

### User Management
- Secure user authentication with JWT (access + refresh tokens)
- Email account activation via async Celery tasks
- Token blacklisting for secure logout
- Follow/unfollow system
- Profile settings

### Social Interactions
- Posts with reactions
- Comments and replies
- User profiles with photo upload
- Polls with voting
- Repost functionality
- Pagination and filtering

### Real-Time Chat
- WebSocket-based messaging via Socket.IO
- Chat member management
- Real-time notification delivery over WebSocket
- Message read receipts

### Notifications
- Real-time WebSocket push notifications
- Notification type filtering (LIKE, COMMENT, FOLLOW, REPOST, etc.)
- Celery-powered email notifications

### Media Management
- User profile picture and post image uploads
- Image validation and compression with Pillow
- Database-backed photo storage

### Advanced Capabilities
- Redis caching layer (user, post, follower list TTL-based caching)
- Rate limiting via slowapi
- Structured logging with structlog
- Request/response timing middleware
- Async-first architecture

## Technology Stack

### Backend
- **Framework:** FastAPI 0.115.4
- **Language:** Python 3.12
- **ORM:** SQLModel 0.0.39 (SQLAlchemy + Pydantic)
- **Database:** PostgreSQL 16 (async via asyncpg)
- **Migrations:** Alembic
- **Validation:** Pydantic v2
- **Caching:** Redis 7
- **Async Tasks:** Celery 5.5.0
- **Real-Time:** Socket.IO (python-socketio)
- **Authentication:** python-jose (JWT) + passlib/bcrypt
- **API Documentation:** Automatic OpenAPI/Swagger

### Real-Time Communication
- Socket.IO over WebSocket
- Redis adapter for multi-process broadcasting
- JWT-based connection authentication
- Real-time notification and chat events

### Code Quality
- **Linting:** Ruff
- **Formatting:** Black
- **Type Checking:** Mypy (strict mode)
- **Pre-commit:** Automated checks via pre-commit hooks
- **Security Scanning:** Bandit, Safety

### Testing
- **Framework:** pytest + pytest-asyncio
- **HTTP Client:** httpx (ASGI transport)
- **Coverage:** pytest-cov

### Deployment
- **Containerization:** Docker (multi-stage build)
- **Orchestration:** Docker Compose (Postgres, Redis, API, Celery worker)
- **CI/CD:** GitHub Actions (lint, typecheck, test, security scan, build)

## Project Structure

```plaintext
QuietSpace-Backend/
├── app/
│   ├── api/            # Route handlers (v1 endpoints)
│   ├── config/         # Database and Redis session factories
│   ├── core/           # Caching, middleware, error handlers
│   ├── enums/          # Shared enums (NotificationType, Role, etc.)
│   ├── models/         # SQLModel database models
│   ├── repositories/   # Data access layer
│   ├── schemas/        # Pydantic request/response schemas
│   ├── services/       # Business logic layer
│   ├── tasks/          # Celery async task definitions
│   ├── utils/          # Utility helpers
│   ├── celery_app.py   # Celery application setup
│   └── main.py         # FastAPI application entry point
├── alembic/            # Database migration scripts
├── scripts/            # Utility scripts (e.g., locust load testing)
├── templates/          # HTML email templates
├── tests/              # Unit, integration, and WebSocket tests
├── legacy/             # Archived Spring Boot source
├── docker-compose.yml  # Local development stack
├── Dockerfile          # Multi-stage production image
└── pyproject.toml      # Project configuration and dependencies
```

## Getting Started

### Prerequisites
- Python 3.12+
- Poetry (for dependency management)
- PostgreSQL 16 (or Docker)
- Redis 7 (or Docker)

### Quick Setup

1. Clone the repository
   ```bash
   git clone https://github.com/thural/QuietSpace-Backend.git
   cd QuietSpace-Backend
   ```

2. Configure environment
   ```bash
   cp .env.example .env
   # Edit .env with your database and SMTP settings
   ```

3. Install dependencies
   ```bash
   poetry install
   ```

4. Start services with Docker Compose
   ```bash
   docker compose up -d postgres redis
   ```

5. Run database migrations
   ```bash
   poetry run alembic upgrade head
   ```

6. Start the development server
   ```bash
   poetry run uvicorn app.main:app --reload --port 8000
   ```

### Run with Docker Compose (full stack)
```bash
docker compose up --build
```

## API Documentation

Interactive OpenAPI documentation available at:
- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

## Testing

```bash
# Run all tests
poetry run pytest

# With coverage
poetry run pytest --cov=app

# Specific test suites
poetry run pytest tests/unit/
poetry run pytest tests/integration/
```

## Code Quality

```bash
# Lint
poetry run ruff check app/ tests/

# Format check
poetry run black --check app/ tests/

# Type check
poetry run mypy app/

# Security scan
poetry run bandit -r app/
poetry run safety scan
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Push and create a pull request
