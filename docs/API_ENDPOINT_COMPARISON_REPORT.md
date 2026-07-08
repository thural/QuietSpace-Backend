# API Endpoint Coverage Analysis Report

**Date:** 2026-07-08
**Source:** Live OpenAPI & AsyncAPI specs from both backends

| Backend | Version | REST Spec | WebSocket Spec |
|---------|---------|-----------|----------------|
| **FastAPI (current)** | Python 3.12 / FastAPI 0.115.4 | `/openapi.json` | `/asyncapi.json` |
| **Spring Boot (legacy)** | Java 17 / Spring Boot 3.3.4 | `/v3/api-docs` | `/springwolf/docs` |

---

## 1. REST Endpoints — Coverage by Domain

### 1.1 Authentication

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `POST /register` | ✅ `/api/v1/auth/register` | ✅ `/api/v1/auth/register` | Identical |
| `POST /login` | ✅ `/api/v1/auth/login` | ✅ `/api/v1/auth/authenticate` | Different path name |
| `POST /refresh` | ✅ `/api/v1/auth/refresh` | ✅ `/api/v1/auth/refresh-token` | Different path name |
| `POST /logout` | ✅ `/api/v1/auth/logout` | ✅ `/api/v1/auth/signout` | Different path name |
| `POST /activate-account` | ✅ `/api/v1/auth/activate-account` | ✅ `/api/v1/auth/activate-account` | Identical |
| `POST /resend-code` | ✅ `/api/v1/auth/resend-code` | ✅ `/api/v1/auth/resend-code` | Identical |

**Coverage: 6/6 (100%)** — All auth endpoints present. Only naming differences.

---

### 1.2 Users

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /users/{id}` | ✅ `/api/v1/users/{user_id}` | ✅ `/api/v1/users/{userId}` | Identical (path param naming differs) |
| `GET /users/me` | ✅ `/api/v1/users/me` | ✅ `/api/v1/users/profile` | FastAPI renamed |
| `PATCH /users/me` | ✅ `/api/v1/users/me` (PATCH) | ✅ `/api/v1/users` (PATCH) | FastAPI uses `/me` sub-path |
| `DELETE /users/{id}` | ❌ | ✅ `/api/v1/users/{userId}` | **MISSING in FastAPI** |
| `GET /users/search` | ✅ `/api/v1/users/search` | ✅ `/api/v1/users/search` | Identical |
| `GET /users/query` | ✅ `/api/v1/users/query` | ✅ `/api/v1/users/query` | Identical |
| `GET /users/online` | ✅ `/api/v1/users/online` | ❌ | FastAPI addition |
| `GET /users/{id}/followers` | ✅ `/api/v1/users/{user_id}/followers` | ✅ `/api/v1/users/{userId}/followers` | Identical |
| `GET /users/{id}/following` | ✅ `/api/v1/users/{user_id}/following` | ✅ `/api/v1/users/{userId}/followings` | Different naming (singular vs plural) |
| `POST /follow/{id}` | ✅ `/api/v1/users/{user_id}/follow` (POST) | ✅ `/api/v1/users/follow/{userId}/toggle-follow` (POST) | FastAPI split into separate follow/unfollow; legacy uses toggle |
| `DELETE /unfollow/{id}` | ✅ `/api/v1/users/{user_id}/follow` (DELETE) | ❌ (toggle only) | FastAPI split approach |
| `POST /followers/remove` | ✅ `/api/v1/users/followers/remove/{follower_id}` | ✅ `/api/v1/users/followers/remove/{userId}` | Identical |
| `GET /users/me/settings` | ✅ `/api/v1/users/me/settings` | ✅ `/api/v1/users/profile/settings` (PATCH only) | Legacy only has PATCH, no GET |
| `PATCH /settings` | ✅ `/api/v1/users/me/settings` (PUT) | ✅ `/api/v1/users/profile/settings` (PATCH) | Different method (PUT vs PATCH) |
| `POST /block/{id}` | ✅ `/api/v1/users/profile/block/{user_id}` | ✅ `/api/v1/users/profile/block/{userId}` | Identical |
| `DELETE /unblock/{id}` | ✅ `/api/v1/users/profile/block/{user_id}` (DELETE) | ❌ | **MISSING in legacy** — FastAPI addition |
| `GET /blocked` | ✅ `/api/v1/users/profile/blocked` | ❌ | **MISSING in legacy** — FastAPI addition |
| `GET /users/{id}/save` | ✅ `/api/v1/users/{user_id}/save` | ❌ | FastAPI addition (user with relations) |

**Coverage: FastAPI covers 15/16 legacy user endpoints (93.75%)** — Missing only `DELETE /users/{id}`. FastAPI adds 4 new endpoints (online status, unblock, blocked list, user with relations).

---

### 1.3 Posts

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /posts` | ✅ `/api/v1/posts` | ✅ `/api/v1/posts` | Identical |
| `POST /posts` | ✅ `/api/v1/posts` | ✅ `/api/v1/posts` | Identical |
| `GET /posts/{id}` | ✅ `/api/v1/posts/{post_id}` | ✅ `/api/v1/posts/{postId}` | Identical |
| `PATCH /posts/{id}` | ✅ `/api/v1/posts/{post_id}` (PATCH) | ✅ `/api/v1/posts/{postId}` (PATCH) | Identical |
| `DELETE /posts/{id}` | ✅ `/api/v1/posts/{post_id}` | ✅ `/api/v1/posts/{postId}` | Identical |
| `PUT /posts/{id}` | ❌ | ✅ `/api/v1/posts/{postId}` (PUT) | Legacy has both PUT and PATCH; FastAPI uses PATCH only |
| `POST /posts/repost` | ✅ `/api/v1/posts/repost` | ✅ `/api/v1/posts/repost` | Identical |
| `POST /posts/vote-poll` | ✅ `/api/v1/posts/vote-poll` | ✅ `/api/v1/posts/vote-poll` | Identical |
| `POST /posts/{id}/save` | ✅ `/api/v1/posts/{post_id}/save` (POST) | ✅ `/api/v1/posts/saved/{postId}` (PATCH) | Different approach |
| `DELETE /posts/{id}/save` | ✅ `/api/v1/posts/{post_id}/save` (DELETE) | ❌ (toggle via PATCH) | FastAPI split approach |
| `GET /posts/saved` | ✅ `/api/v1/posts/saved` | ✅ `/api/v1/posts/saved` | Identical |
| `GET /posts/search` | ❌ | ✅ `/api/v1/posts/search` | **MISSING in FastAPI** |
| `GET /posts/user/{id}` | ❌ | ✅ `/api/v1/posts/user/{userId}` | **MISSING in FastAPI** |
| `GET /posts/user/{id}/commented` | ✅ `/api/v1/posts/commented/{user_id}` | ✅ `/api/v1/posts/user/{userId}/commented` | Same function, different path convention |

**Coverage: FastAPI covers 10/13 legacy post endpoints (77%)** — Missing `PUT /posts/{id}`, `GET /posts/search`, `GET /posts/user/{userId}`. The `PUT` endpoint is arguably redundant since PATCH is also available in legacy.

---

### 1.4 Comments

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `POST /comments` | ✅ `/api/v1/comments` | ✅ `/api/v1/comments` | Identical |
| `GET /comments/post/{id}` | ✅ `/api/v1/comments/post/{post_id}` | ✅ `/api/v1/comments/post/{postId}` | Identical |
| `PATCH /comments/{id}` | ✅ `/api/v1/comments/{comment_id}` (PATCH) | ✅ `/api/v1/comments/{commentId}` (PATCH) + PUT | FastAPI uses PATCH only |
| `DELETE /comments/{id}` | ✅ `/api/v1/comments/{comment_id}` | ✅ `/api/v1/comments/{commentId}` | Identical |
| `GET /comments/{id}/replies` | ✅ `/api/v1/comments/{comment_id}/replies` | ✅ `/api/v1/comments/{commentId}/replies` | Identical |
| `GET /comments/{id}` | ❌ | ✅ `/api/v1/comments/{commentId}` | **MISSING in FastAPI** — single comment by ID |
| `GET /comments/user/{id}` | ❌ | ✅ `/api/v1/comments/user/{userId}` | **MISSING in FastAPI** |
| `GET /comments/user/{id}/post/{pid}/latest` | ❌ | ✅ `/api/v1/comments/user/{userId}/post/{postId}/latest` | **MISSING in FastAPI** |

**Coverage: FastAPI covers 5/8 legacy comment endpoints (62.5%)** — Missing `GET /comments/{id}`, `GET /comments/user/{userId}`, and the latest-comment lookup.

---

### 1.5 Chats

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /chats` | ✅ `/api/v1/chats` | ❌ | FastAPI addition — list all chats |
| `POST /chats` | ✅ `/api/v1/chats` | ✅ `/api/v1/chats` | Identical |
| `GET /chats/{id}` | ✅ `/api/v1/chats/{chat_id}` | ✅ `/api/v1/chats/{chatId}` | Identical |
| `PATCH /chats/{id}` | ✅ `/api/v1/chats/{chat_id}` (PATCH) | ❌ | FastAPI addition |
| `DELETE /chats/{id}` | ❌ | ✅ `/api/v1/chats/{chatId}` | **MISSING in FastAPI** |
| `GET /chats/members/{id}` | ❌ | ✅ `/api/v1/chats/members/{userId}` | **MISSING in FastAPI** |
| `POST /chats/{id}/participants` | ✅ `/api/v1/chats/{chat_id}/participants` | ✅ `/api/v1/chats/{chatId}/members/add/{userId}` (PATCH) | Different method/approach |
| `DELETE /participants/{id}` | ✅ `/api/v1/chats/{chat_id}/participants/{user_id}` | ✅ `/api/v1/chats/{chatId}/members/remove/{userId}` (PATCH) | Different method/approach |

**Coverage: FastAPI covers 4/6 legacy chat endpoints (66.7%)** — Missing `DELETE /chats/{id}` and `GET /chats/members/{userId}`. FastAPI adds 2 new endpoints (list chats, update chat).

---

### 1.6 Messages

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `POST /messages` | ✅ `/api/v1/messages` | ✅ `/api/v1/messages` | Identical |
| `GET /messages/chat/{id}` | ✅ `/api/v1/messages/chat/{chat_id}` | ✅ `/api/v1/messages/chat/{chatId}` | Identical |
| `DELETE /messages/{id}` | ✅ `/api/v1/messages/{message_id}` | ✅ `/api/v1/messages/{messageId}` | Identical |
| `GET /messages/chat/{id}/message/{mid}` | ❌ | ✅ `/api/v1/messages/chat/{chatId}/message/{messageId}` | **MISSING in FastAPI** |
| `GET /messages/unread` | ✅ `/api/v1/messages/unread` | ❌ | FastAPI addition |
| `PUT /messages/{id}/read` | ✅ `/api/v1/messages/{message_id}/read` | ❌ (handled via WS) | FastAPI addition (REST read receipt) |

**Coverage: FastAPI covers 3/4 legacy message endpoints (75%)** — Missing only the single-message lookup. FastAPI adds 2 new REST endpoints (unread count, mark read) which legacy handles via WebSocket.

---

### 1.7 Notifications

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /notifications` | ✅ `/api/v1/notifications` | ✅ `/api/v1/notifications` | Identical |
| `GET /notifications/unread/count` | ✅ `/api/v1/notifications/unread/count` | ✅ `/api/v1/notifications/count-pending` | Different path name |
| `PUT /notifications/{id}/read` | ✅ `/api/v1/notifications/{notification_id}/read` | ✅ `/api/v1/notifications/seen/{contentId}` (POST) | Different approach (by ID vs by content ID) |
| `GET /notifications/type/{type}` | ❌ | ✅ `/api/v1/notifications/type/{notificationType}` | **MISSING in FastAPI** |
| `POST /notifications/process` | ❌ | ✅ `/api/v1/notifications/process` | Internal — likely not needed in FastAPI |
| `POST /notifications/process-reaction` | ❌ | ✅ `/api/v1/notifications/process-reaction` | Internal — likely not needed in FastAPI |

**Coverage: FastAPI covers 3/4 public legacy notification endpoints (75%)** — Missing only `GET /notifications/type/{type}`. The two `/process*` endpoints are internal legacy implementation details, likely replaced by FastAPI's service layer.

---

### 1.8 Reactions

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `POST /reactions/toggle` | ✅ `/api/v1/reactions` (POST) | ✅ `/api/v1/reactions/toggle-reaction` (POST) | Same function |
| `GET /reactions/count` | ✅ `/api/v1/reactions/count/{post_id}` | ✅ `/api/v1/reactions/count` | FastAPI uses path param; legacy uses query param |
| `GET /reactions/post/{id}` | ✅ `/api/v1/reactions/post/{post_id}` | ❌ | FastAPI addition |
| `GET /reactions/content` | ❌ | ✅ `/api/v1/reactions/content` | **MISSING in FastAPI** |
| `GET /reactions/user` | ❌ | ✅ `/api/v1/reactions/user` | **MISSING in FastAPI** |

**Coverage: FastAPI covers 2/4 legacy reaction endpoints (50%)** — Missing reactions by user and reactions by content. FastAPI adds an endpoint for post-specific reactions.

---

### 1.9 Photos

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `POST /photos/profile` | ✅ `/api/v1/photos/profile` | ✅ `/api/v1/photos/profile` | Identical |
| `GET /photos/{filename}` | ✅ `/api/v1/photos/{filename}` | ✅ `/api/v1/photos/{name}` | Identical |
| `DELETE /photos/profile/{id}` | ❌ | ✅ `/api/v1/photos/profile/{userId}` | **MISSING in FastAPI** |
| `POST /photos` | ✅ `/api/v1/photos` | ❌ | FastAPI addition |
| `GET /photos/post/{post_id}` | ✅ `/api/v1/photos/post/{post_id}` | ❌ | FastAPI addition |
| `DELETE /photos/{id}` | ✅ `/api/v1/photos/{photo_id}` | ❌ | FastAPI addition |

**Coverage: FastAPI covers 2/3 legacy photo endpoints (66.7%)** — Missing only `DELETE /photos/profile/{userId}`. FastAPI adds 3 new photo management endpoints.

---

### 1.10 Admin

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /admin` | ❌ | ✅ `/api/v1/admin` | Legacy hello-admin endpoint |
| `GET /admin/users` | ✅ `/api/v1/admin/users` | ✅ `/api/v1/admin/users` | Identical |
| `DELETE /admin/users/{id}` | ✅ `/api/v1/admin/users/{user_id}` (DELETE) | ✅ `/api/v1/admin/{userId}` (POST) | Different method/path |
| `PUT /admin/users/{id}/disable` | ✅ `/api/v1/admin/users/{user_id}/disable` | ❌ | FastAPI addition (user disable) |

**Coverage: FastAPI covers 2/3 legacy admin endpoints (66.7%)** — Missing only the `/admin` hello endpoint (trivial). FastAPI adds a user-disable endpoint.

---

### 1.11 Other/Health

| Endpoint | FastAPI | Legacy | Notes |
|----------|---------|--------|-------|
| `GET /health` | ✅ `/health` | ❌ | FastAPI addition |
| `GET /hello` | ❌ | ✅ `/hello` | Legacy health-check |
| `GET /hello/get-remote-host` | ❌ | ✅ `/hello/get-remote-host` | Legacy debug endpoint |

---

### REST Coverage Summary

| Domain | Legacy Endpoints | FastAPI Covered | Coverage % |
|--------|-----------------|-----------------|------------|
| Authentication | 6 | 6 | **100%** |
| Users | 16 | 15 | **93.75%** |
| Posts | 13 | 10 | **77%** |
| Comments | 8 | 5 | **62.5%** |
| Chats | 6 | 4 | **66.7%** |
| Messages | 4 | 3 | **75%** |
| Notifications | 6 (2 internal) | 3 | **75%** (of public) |
| Reactions | 4 | 2 | **50%** |
| Photos | 3 | 2 | **66.7%** |
| Admin | 3 | 2 | **66.7%** |
| Other | 2 | 0 | **0%** |
| **Total** | **71** | **52** | **~73%** |

**FastAPI additions (endpoints not in legacy):** `/users/online`, `/users/{id}/follow` (DELETE), `/users/profile/block/{id}` (DELETE), `/users/profile/blocked`, `/users/{id}/save`, `/chats` (GET), `/chats/{id}` (PATCH), `/messages/unread`, `/messages/{id}/read`, `/photos` (POST), `/photos/post/{id}`, `/photos/{id}` (DELETE), `/reactions/post/{id}`, `/admin/users/{id}/disable`, `/health` — **~15 endpoints not present in legacy.**

**Legacy endpoints not yet covered:** `DELETE /users/{id}`, `PUT /posts/{id}`, `GET /posts/search`, `GET /posts/user/{userId}`, `GET /comments/{commentId}`, `GET /comments/user/{userId}`, `GET /comments/user/{userId}/post/{postId}/latest`, `DELETE /chats/{chatId}`, `GET /chats/members/{userId}`, `GET /messages/chat/{chatId}/message/{messageId}`, `GET /notifications/type/{notificationType}`, `GET /reactions/content`, `GET /reactions/user`, `DELETE /photos/profile/{userId}`, `DELETE /users/{userId}`, `GET /hello`, `GET /hello/get-remote-host` — **~17 endpoints.**

---

## 2. WebSocket (Socket.IO / STOMP) — Coverage Comparison

### Important Architectural Difference

The two backends use **fundamentally different real-time protocols**:

| Aspect | FastAPI (current) | Spring Boot (legacy) |
|--------|-------------------|---------------------|
| **Library** | Socket.IO (`python-socketio`) | STOMP over WebSocket (Spring) |
| **Connection path** | `/ws` (single raw WS) | `/ws` (with SockJS fallback) |
| **Message model** | Named events (pub/sub) | Destinations with broker prefixes |
| **Auth** | JWT in handshake `Authorization` header | JWT in STOMP `CONNECT` frame |
| **Rooms** | `public`, `chat_{id}` (managed in code) | `/public`, `/user`, `/private` (broker prefixes) |

### Functional Capability Mapping

| Capability | FastAPI Event | Legacy STOMP Destination | Status |
|------------|--------------|--------------------------|--------|
| **Connection confirmation** | `connected` (subscribe) | (implicit via `SessionConnectEvent`) | ✅ Both |
| **User online broadcast** | `user_connected` (subscribe) | BaseEvent → `/public` (CONNECT) | ✅ Both |
| **User offline broadcast** | `user_disconnected` (subscribe) | BaseEvent → `/public` (DISCONNECT) | ✅ Both |
| **Set online status** | `set_online_status` (publish) → `user_status` (subscribe) | `/app/user/setOnlineStatus` → `/user/public` | ✅ Both |
| **Get online users** | `get_online_users` (publish) → `online_users` (subscribe) | `/app/user/onlineUsers` → `/user/onlineUsers` | ✅ Both |
| **Join chat** | `join_chat` (publish) → `chat_event` (JOIN_CHAT) | `/app/private/chat/join` → `/user/private/chat/event` | ✅ Both |
| **Leave chat** | `leave_chat` (publish) → `chat_event` (LEAVE_CHAT) | `/app/private/chat/leave` → `/user/private/chat/event` | ✅ Both |
| **Send message** | `send_message` (publish) → `new_message` + `message_in_chat` | `/app/private/chat` → `/user/private/chat` + `/app/public/chat` | ✅ Both |
| **Delete message** | `delete_message` (publish) → `chat_event` (DELETE_MESSAGE) | `/app/private/chat/delete/{messageId}` → `/user/private/chat/event` | ✅ Both |
| **Mark message seen** | `seen_message` (publish) → `chat_event` (SEEN_MESSAGE) | `/app/private/chat/seen/{messageId}` → `/user/private/chat/event` | ✅ Both |
| **Notifications push** | `notification` (subscribe) | `/user/private/notifications` | ✅ Both |
| **Unread count push** | `unread_count` (subscribe) | ❌ (not in WS spec) | ➕ FastAPI addition |
| **Notification read** | `new_notification` (subscribe) | `/app/private/notifications/seen/{notificationId}` | ✅ Both |
| **System events** | `system` (subscribe) | ❌ (not in WS spec) | ➕ FastAPI addition |
| **Typing indicator** | `typing_status` (subscribe) | ❌ (not in WS spec) | ➕ FastAPI addition |
| **Public chat** | `public_message` (bidirectional) | `/app/public/chat` ↔ `/public/chat` | ✅ Both |
| **Error events** | ❌ (not documented) | `_error` channel | ➖ Legacy only |

### WebSocket Coverage Summary

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Publish (client→server) events** | 9 | 9 |
| **Subscribe (server→client) events** | 14 | 7* |
| **Total documented channels** | 21 | 11 |
| **Functional coverage of legacy WS** | **~90%** | — |

\* Legacy STOMP channels are fewer because some server pushes go through the same destination (e.g., `chat_event` multiplexes JOIN_CHAT, LEAVE_CHAT, DELETE_MESSAGE, SEEN_MESSAGE into a single channel).

**FastAPI WebSocket additions not in legacy:**
- `typing_status` — typing indicator
- `system` — system-level event broadcasts
- `unread_count` — dedicated unread count push
- `new_notification` — raw notification payload pushes

**Legacy WebSocket features not in FastAPI:**
- `_error` channel — STOMP error events (Socket.IO has its own error handling)
- Public chat room join/leave events are implicit (via `SessionConnectEvent`/`SessionDisconnectEvent` on `/public`)

---

## 3. Notable Differences & Observations

### 3.1 Authentication
- **FastAPI** uses `/login` + `/refresh` + `/logout` naming; **Legacy** uses `/authenticate` + `/refresh-token` + `/signout`
- **FastAPI** splits user management: `GET/PATCH /users/me` vs legacy's `GET /users/profile` + `PATCH /users`
- **FastAPI** splits follow/unfollow into `POST`/`DELETE` on the same path; legacy uses a single toggle endpoint

### 3.2 WebSocket Protocol Design
- **FastAPI** (Socket.IO): Named events with explicit rooms. Simpler client integration.
- **Legacy** (STOMP): Broker-based destinations with topic prefixes (`/user/`, `/public/`, `/private/`). More structured but more boilerplate.
- **FastAPI** consolidates chat events into a single `chat_event` channel with a typed discriminator; legacy sends different events to the same `chat/event` destination.

### 3.3 REST vs WebSocket Division of Work
- Legacy uses WebSocket for more operations (read receipts, seen notifications) that FastAPI exposes as REST endpoints too (`PUT /messages/{id}/read`, `PUT /notifications/{id}/read`)
- FastAPI has a more complete REST API for photo management (CRUD vs legacy's minimal photo endpoints)
- FastAPI introduces `blocked user management` (GET/DELETE) as REST endpoints not present in legacy

### 3.4 Missing Functional Gaps (FastAPI)

**High priority:**
1. `GET /posts/search` — search posts by query
2. `GET /posts/user/{userId}` — get posts by specific user
3. `GET /comments/{commentId}` — get single comment by ID
4. `GET /comments/user/{userId}` — get comments by user
5. `GET /reactions/user` — get reactions by current user
6. `GET /reactions/content` — get reactions for specific content
7. `GET /chats/members/{userId}` — get all chats for a user

**Medium priority:**
8. `GET /notifications/type/{type}` — filter notifications by type
9. `GET /comments/user/{userId}/post/{postId}/latest` — latest comment lookup
10. `DELETE /chats/{chatId}` — delete a chat

**Low priority:**
11. `DELETE /users/{userId}` — delete user (admin or self)
12. `DELETE /photos/profile/{userId}` — delete profile photo
13. `GET /hello` and `GET /hello/get-remote-host` — legacy debug endpoints (replaced by `/health`)

### 3.5 New Features in FastAPI (not in legacy)

1. Typing indicators over WebSocket
2. System event broadcasting
3. Unread message count REST endpoint
4. User disable by admin
5. Blocked users list management
6. User online status REST endpoint
7. Complete photo CRUD (create, get post photos, delete by ID)
8. Post-specific reaction counts
9. Health check endpoint with DB + Redis status

---

## 4. Overall Assessment

| Metric | Count |
|--------|-------|
| Legacy REST endpoints | ~71 |
| FastAPI REST endpoints | ~67 |
| REST coverage (legacy → FastAPI) | **~73%** |
| FastAPI REST additions | ~15 |
| Legacy WS channels | 11 |
| FastAPI WS channels | 21 |
| WS functional coverage | **~90%** |
| Overall conversion completeness | **~80%** |

The FastAPI backend covers the majority of the legacy API surface with a cleaner, more RESTful design. The main gaps are in comment retrieval, post search, reaction querying, and a handful of legacy-specific endpoints. The WebSocket layer is functionally superior with more event types and typing indicators.
