# WebSocket (Socket.IO) Implementation Plan

**Backend:** FastAPI (QuietSpace)
**Goal:** Add missing real-time features and apply 2026 enterprise best practices
**Source report:** `docs/WEBSOCKET_ENDPOINT_COMPARISON_REPORT.md`

---

## Phase 1 — Error Handling & Connection Lifecycle

> **Goal:** Provide structured error feedback to clients and optimize connection stability for mobile.

### 1.1 Add `error` Event

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Current** | No error event. Operation failures are silently ignored (e.g., `delete_message` catches `ValueError` and returns nothing) |
| **Target** | Server emits `error` event with `{ code, message, operation, timestamp }` when an operation fails |
| **Effort** | Medium (~1.5 hr) |

**Implementation details:**
- Add `ErrorCode` enum to `app/enums/websocket_event_type.py`:
  ```python
  class ErrorCode(str, Enum):
      AUTH_FAILED = "AUTH_FAILED"
      FORBIDDEN = "FORBIDDEN"
      NOT_FOUND = "NOT_FOUND"
      VALIDATION_ERROR = "VALIDATION_ERROR"
      RATE_LIMITED = "RATE_LIMITED"
      INTERNAL_ERROR = "INTERNAL_ERROR"
  ```
- Create `ErrorEvent` model in `app/models/websocket_event.py`:
  ```python
  class ErrorEvent(BaseModel):
      event_type: str = "error"
      code: ErrorCode
      message: str
      operation: str
      timestamp: datetime
  ```
- Add an `emit_error()` helper to `ConnectionManager` in `app/api/websocket/manager.py`
- Update handlers in `app/api/websocket/handlers.py` to emit `error` on failures instead of silent returns:
  - `authenticate_websocket_token` failure → emit `error{ AUTH_FAILED }`
  - `delete_message` ValueError → emit `error{ FORBIDDEN }` instead of silent return
  - `seen_message` no result → emit `error{ NOT_FOUND }`
  - `join_chat` if user not a member → emit `error{ FORBIDDEN }`
- Add `ERROR` to `WebSocketEventType` enum

**Files to change:**
- `app/enums/websocket_event_type.py` — add `ErrorCode` enum
- `app/models/websocket_event.py` — add `ErrorEvent` model
- `app/api/websocket/manager.py` — add `emit_error()` helper
- `app/api/websocket/handlers.py` — replace silent returns with error emits

---

### 1.2 Configure Heartbeat

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Current** | Socket.IO `AsyncServer` created with defaults (default `ping_interval=25s`, `ping_timeout=20s` for Socket.IO 5.x — verify) |
| **Target** | Explicitly configure `ping_interval=25`, `ping_timeout=60` for mobile-friendly disconnection detection |
| **Effort** | Small (~10 min) |

**Files to change:**
- `app/api/websocket/socketio.py` — add `ping_interval=25, ping_timeout=60` to `AsyncServer()` constructor

---

## Phase 2 — Event Envelope Standardization

> **Goal:** Consistent event structure across all server-emitted events so clients can use a generic handler.

### 2.1 Standardize Bare-Payload Events

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Current** | `user_connected` and `user_disconnected` emit bare `{ user_id: str }` without a typed envelope |
| **Target** | Wrap in a lightweight envelope: `{ event_type: "USER_CONNECTED", user_id, timestamp }` |
| **Effort** | Medium (~1 hr) |

**Implementation details:**
- Refactor `ConnectionManager.connect_user()` and `disconnect_user()` to emit structured payloads:
  ```python
  # Before
  await socketio.emit("user_connected", {"user_id": str(user_id)})

  # After
  await socketio.emit("user_connected", {
      "event_type": "USER_CONNECTED",
      "user_id": str(user_id),
      "timestamp": datetime.now(timezone.utc).isoformat(),
  })
  ```
- Same pattern for `user_disconnected`
- Keep `chat_event` as-is (already has a proper envelope via `EventFactory`)

**Files to change:**
- `app/api/websocket/manager.py` — update `connect_user()` and `disconnect_user()` emit payloads

---

### 2.2 Add `event_version` to BaseEvent

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | No versioning — clients cannot detect schema changes |
| **Target** | Add `event_version: int = 1` to `BaseEvent` model |
| **Effort** | Small (~20 min) |

**Implementation details:**
- Add `event_version` field to `BaseEvent` in `app/models/websocket_event.py`:
  ```python
  class BaseEvent(BaseModel):
      event_version: int = 1
      event_type: WebSocketEventType
      timestamp: datetime
      actor_id: UUID
      data: dict[str, Any] = {}
  ```

**Files to change:**
- `app/models/websocket_event.py` — add `event_version` to `BaseEvent`

---

## Phase 3 — Idempotency & Deduplication

> **Goal:** Prevent duplicate message processing after client reconnection.

### 3.1 Add Client Message ID for Deduplication

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Current** | No dedup support. If a client reconnects and replays buffered events, the same message could be persisted twice |
| **Target** | `send_message`, `delete_message`, `seen_message` accept optional `client_message_id: UUID`. Server stores seen IDs in Redis with TTL and ignores duplicates |
| **Effort** | Medium (~2 hr) |

**Implementation details:**
- Add `client_message_id: str | None = None` field to relevant handler `data` payloads
- Create a Redis-based dedup set in `ConnectionManager`:
  ```python
  async def is_duplicate(self, client_message_id: str) -> bool:
      exists = await redis_client.sismember("ws_dedup", client_message_id)
      if not exists:
          await redis_client.sadd("ws_dedup", client_message_id)
          await redis_client.expire("ws_dedup", 86400)  # 24h TTL
      return bool(exists)
  ```
- Check `is_duplicate()` at the start of `handle_send_message`, `handle_delete_message`, `handle_seen_message` and skip if true

**Files to change:**
- `app/api/websocket/manager.py` — add `is_duplicate()` method
- `app/api/websocket/handlers.py` — add dedup check to relevant handlers

---

## Phase 4 — Scoping & Rate Limiting

> **Goal:** Privacy-conscious presence and protection against abuse.

### 4.1 Scope `get_online_users` to Followings

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | Returns ALL online users — noisy and privacy-invasive |
| **Target** | Returns only online users among the requesting user's followings |
| **Effort** | Small (~30 min) |

**Implementation details:**
- In `handle_get_online_users`, look up the requesting `user_id` (need to track `sid → user_id` mapping — already exists in `active_connections`)
- Fetch the user's followings list from the repository
- Intersect with the global online users set from Redis
- Return only the intersection

**Files to change:**
- `app/api/websocket/handlers.py` — update `handle_get_online_users` to scope by followings
- `app/repositories/user.py` — ensure `get_following_ids()` exists

---

### 4.2 Add WebSocket Rate Limiting

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | No rate limits on WS events — abuse possible (spam messages, rapid typing events) |
| **Target** | Token bucket rate limiter per user per event type using Redis |
| **Effort** | Medium (~1.5 hr) |

**Implementation details:**
- Create a WS rate limiter in `app/api/websocket/rate_limiter.py`:
  ```python
  class WebSocketRateLimiter:
      def __init__(self, redis):
          self.redis = redis

      async def check(self, user_id: UUID, event: str, max_burst: int, per_seconds: int) -> bool:
          key = f"ws_rate:{user_id}:{event}"
          current = await self.redis.incr(key)
          if current == 1:
              await self.redis.expire(key, per_seconds)
          return current <= max_burst
  ```
- Apply limits in handlers:
  - `send_message`: max 10 per 10 seconds
  - `public_message`: max 5 per 10 seconds
  - `set_online_status`: max 5 per 60 seconds
  - `typing_status`: max 30 per 60 seconds (handled by throttling in Phase 5)

**Files to change:**
- `app/api/websocket/rate_limiter.py` — new file
- `app/api/websocket/handlers.py` — add rate limit checks

---

### 4.3 Add Latency Measurement

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | No performance tracking — clients have no way to measure round-trip latency |
| **Target** | Accept optional `client_timestamp` in publish payloads; include `server_timestamp` in response events |
| **Effort** | Small (~20 min) |

**Implementation details:**
- In relevant handler responses (e.g., `connected`, `new_message`, `chat_event`), include `server_timestamp` when the source publish had `client_timestamp`
- Clients can then calculate `latency = server_timestamp - client_timestamp`

**Files to change:**
- `app/api/websocket/handlers.py` — pass through `client_timestamp` where present, add `server_timestamp` to responses

---

## Phase 5 — Feature Enhancements

> **Goal:** Fill remaining feature gaps from legacy and industry standards.

### 5.1 Persist Public Chat Messages

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | Public messages are fire-and-forget broadcast only |
| **Target** | Persist to a designated "public chat" record so history is available on reconnect |
| **Effort** | Medium (~1.5 hr) |

**Implementation details:**
- Use the existing `Message` model with a reserved/sentinel `chat_id` for the public room (or create a `PublicMessage` model)
- In `handle_public_message`, persist the message to DB before broadcasting
- Add a `MessageService.send_public_message()` method

**Files to change:**
- `app/services/message_service.py` — add `send_public_message()` method
- `app/api/websocket/handlers.py` — persist in `handle_public_message`
- Alembic migration — if using a separate table/model

---

### 5.2 Throttle Typing Indicators

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | No server-side throttling — clients can fire `set_online_status` rapidly |
| **Target** | Server throttles `typing_status` broadcasts: max 1 per 2 seconds per user per chat |
| **Effort** | Small (~30 min) |

**Implementation details:**
- Add a `last_typing_broadcast: Dict[Tuple[UUID, UUID], float]` to `ConnectionManager` tracking `(user_id, chat_id) → timestamp`
- In handlers, skip broadcast if last emit was < 2 seconds ago

**Files to change:**
- `app/api/websocket/manager.py` — add throttle tracking
- `app/api/websocket/handlers.py` — add throttle check in typing handler

---

### 5.3 Consolidate Notification Events

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | Two separate events: `notification` (typed `NotificationEvent` envelope) and `new_notification` (raw `Notification` model dump) |
| **Target** | Deprecate `new_notification`, consolidate into single `notification` event. The `NotificationEvent` envelope already has `event_type: NOTIFICATION` vs `NOTIFICATION_READ` to distinguish create vs read |
| **Effort** | Small (~20 min) |

**Implementation details:**
- Update `NotificationService.create_notification()` in `app/services/notification_service.py` to emit `notification` instead of `new_notification` (already does — the `new_notification` event is emitted elsewhere, check `websocket_service.py`)
- Update `websocket_service.py` line 19 to emit `notification` instead of `new_notification`
- Remove `new_notification` from AsyncAPI spec

**Files to change:**
- `app/services/notification_service.py` — verify event name
- `app/services/websocket_service.py` — change `new_notification` → `notification`
- `app/api/websocket/asyncapi.yaml` — remove `new_notification` channel

---

## Phase 6 — AsyncAPI Spec Updates

> **Goal:** Keep the spec in sync with all implementation changes.

### 6.1 Document `error` Event Channel

| Item | Detail |
|------|--------|
| **Effort** | Small (~15 min) |
| **What** | Add `error` subscribe channel with `ErrorEvent` payload schema to `asyncapi.yaml` |
| **Files** | `app/api/websocket/asyncapi.yaml` |

### 6.2 Add `event_version` to Component Schemas

| Item | Detail |
|------|--------|
| **Effort** | Small (~10 min) |
| **What** | Add `event_version` property to `BaseEvent` schema and set default to 1 |
| **Files** | `app/api/websocket/asyncapi.yaml` |

### 6.3 Document Heartbeat in Server Extension

| Item | Detail |
|------|--------|
| **Effort** | Small (~5 min) |
| **What** | Add `x-heartbeat-interval` and `x-heartbeat-timeout` custom properties to the server object |
| **Files** | `app/api/websocket/asyncapi.yaml` |

---

## Phase 7 — Tests

> **Goal:** Verify all changes with unit and integration tests.

### 7.1 Unit Tests for Error Event

| Item | Detail |
|------|--------|
| **Effort** | Medium (~1 hr) |
| **Scope** | Test that `handle_connect` with bad token emits `error`, `handle_delete_message` with wrong user emits `error`, `handle_seen_message` with bad ID emits `error` |
| **Files** | `tests/unit/test_websocket_handlers.py` |

### 7.2 Unit Tests for Envelope Changes

| Item | Detail |
|------|--------|
| **Effort** | Small (~20 min) |
| **Scope** | Test that `user_connected`/`user_disconnected` include `event_type` and `timestamp` fields |
| **Files** | `tests/unit/test_websocket_manager.py` |

### 7.3 Integration Tests for Scoping + Rate Limiting

| Item | Detail |
|------|--------|
| **Effort** | Medium (~1.5 hr) |
| **Scope** | Test that `get_online_users` returns only followings, rate limited user gets no response |
| **Files** | `tests/integration/test_websocket_integration.py` |

### 7.4 Tests for Public Chat Persistence

| Item | Detail |
|------|--------|
| **Effort** | Medium (~1 hr) |
| **Scope** | Test that `public_message` persists to DB, can be retrieved on reconnect |
| **Files** | `tests/integration/test_websocket_integration.py` |

---

## Implementation Order Summary

```
Phase 1 ──► Phase 3 ──► Phase 2 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7
  (error +       (dedup)      (envelope      (scoping +      (features)     (spec      (tests)
   heartbeat)                  consistency)    rate limit)                    updates)
```

| Phase | Focus | Steps | Est. Effort |
|-------|-------|-------|-------------|
| **1** | Error handling + heartbeat | 2 | ~1.6 hr |
| **2** | Event envelope consistency | 2 | ~1.3 hr |
| **3** | Idempotency / dedup | 1 | ~2 hr |
| **4** | Scoping + rate limiting + latency | 3 | ~2.3 hr |
| **5** | Feature enhancements | 3 | ~2 hr |
| **6** | AsyncAPI spec updates | 3 | ~0.5 hr |
| **7** | Tests | 4 | ~3.7 hr |
| **Total** | | **18** | **~13.4 hr** |

---

## How to Use This Plan

Each step should be implemented and committed independently:

```bash
# Example workflow for Step 1.1
# 1. Add ErrorCode enum
# 2. Add ErrorEvent model
# 3. Add emit_error to manager
# 4. Update handlers to emit error
# 5. Update AsyncAPI spec
# 6. Write tests
# 7. Run full test suite
poetry run pytest
# 8. Commit
git add -A && git commit -m "Add WS error event with structured error codes"
```
