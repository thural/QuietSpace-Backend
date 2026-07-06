# FastAPI Migration Completion Plan

## Phase 0: Quick Fixes (Critical blockers)

| # | Task | Details | Status |
|---|------|---------|--------|
| 0.1 | **Fix Dockerfile** | Generate `poetry.lock` during build via `poetry lock` | ✅ |
| 0.2 | **Implement WebSocket `handle_send_message`** | Replace `pass` with actual message persistence + broadcast | ✅ |
| 0.3 | **Wire structlog as middleware** | Configure `structlog` processors in lifespan, add request logging middleware | ✅ |
| 0.4 | **Wire slowapi rate limiting** | Add `Limiter` + `add_exception_handler` in `main.py`, apply to endpoints | ✅ |
| 0.5 | **Fix `StatusType` duplication** | Remove duplicate in `role.py`, keep only in `status_type.py` | ✅ |

## Phase 1: Database & Migrations

| # | Task | Details | Status |
|---|------|---------|--------|
| 1.1 | **Initialize Alembic** | Run `alembic init`, configure `env.py` for async + SQLModel metadata, create initial migration | ✅ |
| 1.2 | **Create `app/config/database.py`** | Extract engine + session factory from `main.py` into dedicated config | ✅ |
| 1.3 | **Create `app/config/redis.py`** | Redis client factory for caching, Celery, Socket.IO | ✅ |
| 1.4 | **Implement relationship loading strategies** | Add `selectinload`/`joinedload` in repository query methods | ✅ |

## Phase 2: Security & Cross-Cutting

| # | Task | Details | Status |
|---|------|---------|--------|
| 2.1 | **Implement token blacklisting** | Wire `Token` model into auth flow; check blacklist in `get_current_user` | ✅ |
| 2.2 | **Create `app/core/audit.py`** | Auto-populate `created_by`/`updated_by` from current user context | ✅ |
| 2.3 | **Create `app/core/middleware.py`** | Request logging, timing, structured logging middleware | ✅ |
| 2.4 | **Add `Annotated` DI aliases** | `AsyncSessionDep`, `CurrentUserDep` in `app/api/deps.py` | ✅ |
| 2.5 | **Create `.pre-commit-config.yaml`** | With ruff, black, mypy hooks | ✅ |

## Phase 3: Auth & User Features

| # | Task | Details | Status |
|---|------|---------|--------|
| 3.1 | **Implement account activation endpoint** | `POST /api/v1/auth/activate-account` with activation code verification | ✅ |
| 3.2 | **Implement refresh token distinct from access token** | Separate long-lived refresh tokens with rotation | ✅ |
| 3.3 | **Implement follow/unfollow system** | `POST /api/v1/users/{id}/follow`, `DELETE /api/v1/users/{id}/follow`, add `followers`/`followings` M2M on User model | ✅ |
| 3.4 | **Implement profile settings endpoints** | `GET/PUT /api/v1/users/me/settings` | ✅ |
| 3.5 | **Add admin DELETE user** | `DELETE /api/v1/admin/users/{id}` | ✅ |

## Phase 4: Content & Media

| # | Task | Details | Status |
|---|------|---------|--------|
| 4.1 | **Migrate Poll/PollOption models** | Create `Poll` and `PollOption` SQLModel entities + schemas + endpoints | ✅ |
| 4.2 | **Implement saved posts endpoints** | `POST/DELETE /api/v1/posts/{id}/save`, `GET /api/v1/users/me/saved-posts` | ✅ |
| 4.3 | **Implement photo profile upload** | `POST /api/v1/photos/profile` | ✅ |
| 4.4 | **Implement photo serving endpoint** | `GET /api/v1/photos/{filename}` | ✅ |
| 4.5 | **Add reaction count endpoint** | `GET /api/v1/reactions/count` | ✅ |

## Phase 5: Real-Time & Notifications

| # | Task | Details | Status |
|---|------|---------|--------|
| 5.1 | **Wire Socket.IO Redis adapter** | `AsyncRedisManager` for horizontal scaling | ✅ |
| 5.2 | **Wire real-time notification broadcasts** | Emit Socket.IO events from `NotificationService` | ✅ |
| 5.3 | **Wire Celery email notifications** | Call Celery task from notification service for offline users | ✅ |
| 5.4 | **Implement chat member management** | Add/remove participants endpoints | ✅ |
| 5.5 | **Add notification type filtering** | `GET /api/v1/notifications?type={type}` | ✅ |

## Phase 6: Testing

| # | Task | Details | Status |
|---|------|---------|--------|
| 6.1 | **Write integration tests for all 10 endpoint modules** | Auth, users, posts, comments, chats, messages, notifications, reactions, photos, admin | ✅ 30 tests passing |
| 6.2 | **Write WebSocket integration tests** | Socket.IO connect/disconnect/send_message | ✅ 4 tests |
| 6.3 | **Write unit tests for services/repositories** | Core business logic coverage | ✅ 54 unit tests passing |
| 6.4 | **Add security tests** | JWT expiration, token blacklist, rate limiting | ✅ 10 tests |
| 6.5 | **Configure bandit + safety** | Add to CI/CD workflow | ✅ |

## Phase 7: Production Polish

| # | Task | Details | Status |
|---|------|---------|--------|
| 7.1 | **Add Redis caching layer** | Cache for frequently-read data (posts, user profiles) | ✅ |
| 7.2 | **Add `EmailTemplateName` enum** | Replace hardcoded strings in email service | ✅ |
| 7.3 | **Add missing enums** | `Permission`, `EntityType`, `EventType` | ✅ |
| 7.4 | **Performance benchmarking** | Load test critical endpoints with locust | ✅ |
| 7.5 | **Add repost functionality** | POST /api/v1/posts/repost + cascading delete | ✅ |
