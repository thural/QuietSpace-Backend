# Usage Guide

## Prerequisites

- Python 3.12+
- PostgreSQL 16+
- Redis 7+
- Docker & Docker Compose (optional)

## Running with Docker Compose

```bash
docker compose up -d
```

This starts PostgreSQL, Redis, the API server, and a Celery worker. The API is exposed at `http://localhost:8000`.

To stop:

```bash
docker compose down
```

To restart only the API container after code changes:

```bash
docker compose restart api
```

## Running without Docker (Development Mode)

### 1. Start dependencies

Ensure PostgreSQL and Redis are running locally (or via Docker):

```bash
docker compose up -d postgres redis
```

### 2. Install dependencies

```bash
poetry install
```

### 3. Set environment variables

Copy and adjust the example below:

```bash
export DATABASE_URL=postgresql+asyncpg://test:test@localhost:5432/quietspace
export REDIS_URL=redis://localhost:6379/0
export SECRET_KEY=dev-secret-key
export DEBUG=True
```

### 4. Run database migrations

```bash
alembic upgrade head
```

### 5. Start the server

```bash
poetry run uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The `--reload` flag enables auto-restart on file changes.

## Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `DATABASE_URL` | `postgresql+asyncpg://test:test@localhost:5432/quietspace` | Yes | PostgreSQL async connection string |
| `REDIS_URL` | `redis://localhost:6379/0` | Yes | Redis connection string |
| `SECRET_KEY` | — | **Yes** | JWT signing secret (must be unique per deployment) |
| `ALGORITHM` | `HS256` | No | JWT algorithm |
| `ACCESS_TOKEN_EXPIRE_MINUTES` | `10` | No | JWT access token TTL |
| `REFRESH_TOKEN_EXPIRE_DAYS` | `7` | No | JWT refresh token TTL |
| `SMTP_HOST` | `smtp.example.com` | Yes* | SMTP server (*required for email features) |
| `SMTP_PORT` | `587` | Yes* | SMTP port |
| `SMTP_USER` | — | Yes* | SMTP username |
| `SMTP_PASSWORD` | — | Yes* | SMTP password |
| `FRONTEND_URL` | `http://localhost:3000` | Yes | CORS allowed origin |
| `MAX_UPLOAD_SIZE` | `3145728` | No | Max upload size in bytes (default 3 MB) |
| `DEBUG` | `False` | No | Enable debug mode |
| `FEATURE_RATE_LIMITING` | `True` | No | Enable/disable rate limiting |
| `FEATURE_WS_NOTIFICATIONS` | `True` | No | Enable/disable WebSocket notifications |
| `FEATURE_SOFT_DELETE` | `True` | No | Enable/disable soft delete |

## REST API

All REST endpoints are prefixed with `/api/v1`. The API is described via OpenAPI 3.1.

### Module Overview

| Prefix | Description |
|---|---|
| `/api/v1/auth` | Registration, login, token refresh, logout, account activation |
| `/api/v1/users` | User profiles, search, follow/unfollow, block/unblock, settings |
| `/api/v1/posts` | Post CRUD, repost, save/unsave, search, polls |
| `/api/v1/comments` | Comment CRUD, replies |
| `/api/v1/chats` | Chat creation, management, participants |
| `/api/v1/messages` | Send messages, read receipts, unread count |
| `/api/v1/notifications` | List, filter, mark as read (single and batch) |
| `/api/v1/reactions` | Add/remove reactions, count, list by content/user |
| `/api/v1/photos` | Photo upload, profile photo, post photo listing |
| `/api/v1/admin` | Admin user management (list, disable, delete) |

## WebSocket / Socket.IO

The WebSocket endpoint is at `/ws` (handled by python-socketio).

### Client-to-Server Events

| Event | Description |
|---|---|
| `connect` | Authenticate with `Bearer <token>` in `HTTP_AUTHORIZATION` header |
| `disconnect` | Client disconnects |
| `join_chat` | Join a chat room by `chat_id` |
| `leave_chat` | Leave a chat room |
| `send_message` | Send a message in a chat |
| `delete_message` | Delete a message (by `message_id`) |
| `seen_message` | Mark a message as read |
| `set_online_status` | Update online presence |
| `get_online_users` | Get online users from your followings |
| `typing_status` | Broadcast typing indicator |
| `public_message` | Send a message to the public room |

### Server-to-Client Events

| Event | Description |
|---|---|
| `connected` | Confirmation of successful connection |
| `new_message` | New message delivered to sender/recipient |
| `message_in_chat` | Broadcast of new message to all chat members |
| `chat_event` | Chat lifecycle events (join, leave, delete message, seen) |
| `error` | Structured error with `code`, `message`, `operation` fields |
| `user_status` | Online/offline status change |
| `typing_status` | Typing indicator |
| `notification` | Real-time notification push |
| `unread_count` | Updated unread notification count |
| `online_users` | Response to `get_online_users` |
| `public_message` | Public room message broadcast |

## Documentation Endpoints

| URL | Full URL | Type |
|---|---|---|
| `/docs` | http://localhost:8000/docs | Swagger UI — REST API |
| `/redoc` | http://localhost:8000/redoc | ReDoc — REST API |
| `/openapi.json` | http://localhost:8000/openapi.json | OpenAPI 3.1 JSON schema |
| `/asyncapi` | http://localhost:8000/asyncapi | AsyncAPI UI — WebSocket/Socket.IO events |
| `/asyncapi.json` | http://localhost:8000/asyncapi.json | AsyncAPI 2.6.0 JSON spec |

## Health Check

```bash
curl http://localhost:8000/health
```

Returns `200 OK` with JSON `{"status":"healthy","database":"healthy","redis":"healthy","version":"1.0.0"}` when all services are operational.
