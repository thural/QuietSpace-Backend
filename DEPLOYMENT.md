# Deployment Guide

## Prerequisites

- Python 3.12+
- PostgreSQL 16+
- Redis 7+
- Docker & Docker Compose (optional)

## Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `DATABASE_URL` | `postgresql+asyncpg://...` | Yes | PostgreSQL async connection string |
| `REDIS_URL` | `redis://localhost:6379/0` | Yes | Redis connection string |
| `REDIS_POOL_SIZE` | `20` | No | Max Redis connections in pool |
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
| `DEBUG` | `False` | No | Enable debug mode (SQLAlchemy echo, console logs) |
| `FEATURE_RATE_LIMITING` | `True` | No | Enable/disable rate limiting middleware |
| `FEATURE_WS_NOTIFICATIONS` | `True` | No | Enable/disable WebSocket notification broadcasts |
| `FEATURE_SOFT_DELETE` | `True` | No | Enable/disable soft delete for messages |

## Database Migrations

### Migration Chain (apply in order)

```
d8174bcf9606 (initial)
  └── 258cffccf9de (add repost fields)
       └── 0b0ea170c0a7 (add blocked_users table)
            └── c008ed6aecb7 (add comment depth + composite index)
                 └── add_poll_tables (polls, poll_options, poll_votes)
                      └── add_user_name_indexes (indexes on user.firstname, user.lastname)
                           └── add_activation_code_columns (activation_code + expires_at)
                                └── add_message_deleted_at (soft delete column)
```

### Apply Migrations

```bash
alembic upgrade head
```

### Rollback (one step at a time)

```bash
# Roll back one migration
alembic downgrade -1

# Roll back to a specific revision
alembic downgrade <revision_id>
```

Rollback sequence (reverse order):
```
add_message_deleted_at
  └── add_activation_code_columns
       └── add_user_name_indexes
            └── add_poll_tables
                 └── c008ed6aecb7
                      └── 0b0ea170c0a7
                           └── 258cffccf9de
                                └── d8174bcf9606
```

### Migration Window Checklist

1. [ ] Test migrations on staging environment
2. [ ] Verify recursive CTE queries on production-like data
3. [ ] Schedule during low-traffic period
4. [ ] Take database snapshot before migrating
5. [ ] Run `alembic upgrade head`
6. [ ] Verify application health via `/health` endpoint
7. [ ] Monitor error logs for 15 minutes post-deploy

## Docker Deployment

```bash
# Build and start all services
docker compose up --build -d

# Check health
curl http://localhost:8000/health

# View logs
docker compose logs -f api
```

### Services

| Service | Image | Port | Description |
|---|---|---|---|
| `postgres` | postgres:16-alpine | 5432 | Primary database |
| `redis` | redis:7-alpine | 6379 | Cache, rate limiting, WS pub/sub |
| `api` | (built) | 8000 | FastAPI application (uvicorn) |
| `celery` | (built) | — | Async task worker |

## Monitoring

### Health Endpoint

`GET /health` returns:

```json
{
  "status": "healthy",
  "database": "healthy",
  "redis": "healthy",
  "version": "1.0.0"
}
```

Docker HEALTHCHECK runs every 30 seconds against this endpoint.

### Logging

- Structured JSON logging in production (`DEBUG=False`)
- Human-readable console logging in development (`DEBUG=True`)
- Rate limit violations logged at `WARNING` level
- HTTP request summary logged at `INFO` level

### Key Log Events

| Event | Level | Location |
|---|---|---|
| Application startup/shutdown | INFO | `app/main.py` |
| HTTP request completed | INFO | `app/main.py` (middleware) |
| Rate limit exceeded | WARNING | `app/main.py` |
| WebSocket connected/disconnected | INFO | `app/api/websocket/handlers.py` |
| Message soft deleted | INFO | `app/services/message_service.py` |

## Rollback Plan

### Application Rollback

```bash
# Roll back to previous Docker image
docker compose down
docker compose up --build -d
```

### Database Rollback

```bash
# Identify current revision
alembic current

# Roll back one step
alembic downgrade -1

# Verify application functions with downgraded schema
# Run tests against the downgraded schema
```

### Full Rollback Procedure

1. Stop the application
2. Roll back database migrations (one step at a time)
3. Re-deploy previous application version
4. Verify health endpoint
5. Monitor logs for errors

## Architecture Notes

- **Recursive CTEs**: Comment threads use recursive CTEs with a max depth of 10. Test with production comment volumes.
- **Soft Deletes**: Messages use `deleted_at` timestamp. All queries filter out soft-deleted records by default. Admin endpoints accept `include_deleted` parameter.
- **Cursor Pagination**: Social feeds (posts, notifications, messages) use cursor-based pagination. Search and admin endpoints use offset-based pagination.
- **Rate Limiting**: slowapi with Redis backend. Limits are per-IP. Categories: auth (5/min), content (10/min), sensitive (10/min), resend-code (3/5min).
