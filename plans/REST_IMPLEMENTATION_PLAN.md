# REST Endpoint Implementation Plan

**Backend:** FastAPI (QuietSpace)
**Goal:** Achieve full parity with legacy Spring Boot REST API + apply 2026 enterprise best practices
**Source reports:** `docs/API_ENDPOINT_COMPARISON_REPORT.md`, `docs/REST_ENDPOINT_COMPARISON_REPORT.md`

---

## Phase 1 — High-Impact Missing Endpoints

> **Goal:** Close critical user-facing gaps needed for frontend parity with legacy.

### 1.1 `GET /api/v1/posts/user/{user_id}` — Get Posts by User

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Legacy path** | `/api/v1/posts/user/{userId}` |
| **Effort** | Small (~30 min) |
| **What** | Paginated list of posts authored by a specific user |
| **Dependencies** | `PostRepository.get_by_user_id()` exists? If not, add a query method |
| **Acceptance** | Returns `200` with paginated posts, filters by `user_id`, supports cursor pagination |

**Files to modify:**
- `app/repositories/post.py` — add `get_by_user_id()` method
- `app/services/post_service.py` — add service method (or reuse existing)
- `app/api/v1/posts.py` — add new route

---

### 1.2 `GET /api/v1/posts/search?q=...` — Post Search

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Legacy path** | `/api/v1/posts/search` |
| **Effort** | Medium (~1–2 hr) |
| **What** | Full-text search over post content |
| **Options** | A) PostgreSQL `tsvector`/`ts_query` (no extra infra), B) Meilisearch/Elasticsearch (better but more infra), C) Simple `ILIKE` (acceptable for MVP) |
| **Dependencies** | Search strategy decision, potentially a GIN index on post content |
| **Acceptance** | `GET /api/v1/posts/search?q=keyword` returns matching posts, paginated |

**Files to modify:**
- `app/repositories/post.py` — add `search()` method with chosen strategy
- `app/services/post_service.py` — add search service method
- `app/api/v1/posts.py` — add search route
- Alembic migration — if adding GIN index

---

### 1.3 `GET /api/v1/comments/{comment_id}` — Get Comment by ID

| Item | Detail |
|------|--------|
| **Priority** | 🔴 High |
| **Legacy path** | `/api/v1/comments/{commentId}` |
| **Effort** | Small (~15 min) |
| **What** | Single comment retrieval by primary key |
| **Dependencies** | `CommentRepository.get()` (already exists via `BaseRepository`) |
| **Acceptance** | Returns `200` with comment JSON, `404` if not found |

**Files to modify:**
- `app/services/comment_service.py` — add `get_by_id()` method
- `app/api/v1/comments.py` — add `GET /{comment_id}` route

---

### 1.4 `GET /api/v1/comments/user/{user_id}` — Get Comments by User

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/comments/user/{userId}` |
| **Effort** | Small (~20 min) |
| **What** | Paginated list of comments authored by a specific user |
| **Dependencies** | Need `get_by_user_id()` in comment repository |
| **Acceptance** | Returns `200` with paginated comments filtered by author |

**Files to modify:**
- `app/repositories/comment.py` — add `get_by_user_id()` method
- `app/services/comment_service.py` — add service method
- `app/api/v1/comments.py` — add `GET /user/{user_id}` route

---

### 1.5 `GET /api/v1/reactions/user` — Get Reactions by Current User

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/reactions/user` |
| **Effort** | Small (~20 min) |
| **What** | List reactions made by the authenticated user, optionally filtered by content type |
| **Dependencies** | `ReactionRepository` needs a `get_by_user()` method |
| **Acceptance** | Returns `200` with paginated reactions for current user |

**Files to modify:**
- `app/repositories/reaction.py` — add `get_by_user()` method
- `app/services/reaction_service.py` — add service method
- `app/api/v1/reactions.py` — add `GET /user` route

---

### 1.6 `GET /api/v1/notifications?type={type}` — Filter Notifications by Type

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/notifications/type/{notificationType}` |
| **Effort** | Small (~20 min) |
| **What** | Query parameter `type` on existing `GET /notifications` endpoint |
| **Dependencies** | `NotificationRepository.get_by_user()` should support optional `type` filter |
| **Acceptance** | `GET /api/v1/notifications?type=LIKE` returns only LIKE notifications |

**Files to modify:**
- `app/repositories/notification.py` — add optional `type` parameter to `get_by_user()`
- `app/services/notification_service.py` — pass filter to repository
- `app/api/v1/notifications.py` — add `type` query parameter to existing route

---

## Phase 2 — RESTful Improvements

> **Goal:** Align endpoint structure with 2026 enterprise REST best practices.

### 2.1 Nest Comments Under Posts

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | `POST /api/v1/comments`, `GET /api/v1/comments/post/{post_id}` |
| **Target** | `POST /api/v1/posts/{post_id}/comments`, `GET /api/v1/posts/{post_id}/comments` |
| **Effort** | Medium (~1–2 hr) |
| **Backward compat** | Keep old paths as redirects or deprecate with warning |
| **Acceptance** | Comments are sub-resources of posts in the URL structure |

**Files to modify:**
- `app/api/v1/posts.py` — add nested comment routes under post paths
- `app/api/v1/comments.py` — optionally add redirect/deprecation for old paths

---

### 2.2 Nest Messages Under Chats

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | `POST /api/v1/messages`, `GET /api/v1/messages/chat/{chat_id}` |
| **Target** | `POST /api/v1/chats/{chat_id}/messages`, `GET /api/v1/chats/{chat_id}/messages` |
| **Effort** | Medium (~1–2 hr) |
| **Backward compat** | Keep old paths as redirects |
| **Acceptance** | Messages are sub-resources of chats |

**Files to modify:**
- `app/api/v1/chats.py` — add nested message routes under chat paths
- `app/api/v1/messages.py` — optionally add redirect/deprecation

---

### 2.3 Nest Reactions Under Posts

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | `GET /api/v1/reactions/count/{post_id}`, `GET /api/v1/reactions/post/{post_id}` |
| **Target** | `GET /api/v1/posts/{post_id}/reactions/count`, `GET /api/v1/posts/{post_id}/reactions` |
| **Effort** | Small (~30 min) |
| **Backward compat** | Keep old paths as redirects |
| **Acceptance** | Reaction endpoints are nested under posts |

**Files to modify:**
- `app/api/v1/posts.py` — add nested reaction routes
- `app/api/v1/reactions.py` — add redirects for old paths

---

### 2.4 Fix Followers Remove Method

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Current** | `POST /api/v1/users/followers/remove/{follower_id}` |
| **Target** | `DELETE /api/v1/users/{user_id}/followers/{follower_id}` |
| **Effort** | Small (~30 min) |
| **Acceptance** | Uses DELETE method (idempotent), RESTful path with user + follower IDs |

**Files to modify:**
- `app/api/v1/users.py` — add new route, deprecate old one

---

### 2.5 Fix Block Path to Use `/me/`

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | `/api/v1/users/profile/block/{user_id}` |
| **Target** | `/api/v1/users/me/block/{user_id}` |
| **Effort** | Small (~20 min) |
| **Acceptance** | Block endpoints use `/me/` convention consistently |

**Files to modify:**
- `app/api/v1/users.py` — add new routes, redirect old ones

---

### 2.6 Rename `/users/{id}/save` Endpoint

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | `GET /api/v1/users/{user_id}/save` |
| **Problem** | Path name `save` sounds like an action (POST) not a GET resource |
| **Target** | `GET /api/v1/users/{user_id}/profile` or `GET /api/v1/users/{user_id}/detail` |
| **Effort** | Small (~15 min) |
| **Acceptance** | Clear, descriptive path name |

**Files to modify:**
- `app/api/v1/users.py` — rename route

---

## Phase 3 — Remaining Missing Endpoints

> **Goal:** Full legacy parity — all remaining endpoints from Spring Boot.

### 3.1 `GET /api/v1/messages/{message_id}` — Single Message

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/messages/chat/{chatId}/message/{messageId}` |
| **Effort** | Small (~20 min) |
| **What** | Retrieve a single message by ID |
| **Acceptance** | Returns `200` with message JSON, `404` if not found |

**Files to modify:**
- `app/api/v1/messages.py` — add `GET /{message_id}` route

---

### 3.2 `DELETE /api/v1/chats/{chat_id}` — Delete Chat

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/chats/{chatId}` (DELETE) |
| **Effort** | Small (~30 min) |
| **What** | Soft-delete a chat (set `deleted_at`), verify user is a member |
| **Acceptance** | Returns `204`, chat marked as deleted in DB |

**Files to modify:**
- `app/repositories/chat.py` — ensure soft-delete support
- `app/services/chat_service.py` — add `delete_chat()` method
- `app/api/v1/chats.py` — add `DELETE /{chat_id}` route

---

### 3.3 `GET /api/v1/comments/user/{user_id}/post/{post_id}/latest` — Latest Comment

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Legacy path** | `/api/v1/comments/user/{userId}/post/{postId}/latest` |
| **Effort** | Small (~20 min) |
| **What** | Get the most recent comment by a user on a specific post |
| **Acceptance** | Returns `200` with single comment or `null` |

**Files to modify:**
- `app/repositories/comment.py` — add `get_latest_by_user_on_post()` method
- `app/services/comment_service.py` — add service method
- `app/api/v1/comments.py` — add route

---

### 3.4 `GET /api/v1/reactions/content` — Reactions for Content

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Legacy path** | `/api/v1/reactions/content` |
| **Effort** | Small (~20 min) |
| **What** | Get reactions for specific content (post or comment) by content type + ID |
| **Acceptance** | `GET /api/v1/reactions?content_type=post&content_id={id}` returns reactions |

**Files to modify:**
- `app/repositories/reaction.py` — add `get_by_content()` method
- `app/services/reaction_service.py` — add service method
- `app/api/v1/reactions.py` — add query-param-based route (or make existing `/post/{id}` generic)

---

### 3.5 `DELETE /api/v1/users/{user_id}` — Delete User

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Legacy path** | `/api/v1/users/{userId}` (DELETE) |
| **Effort** | Small (~30 min) |
| **What** | Soft-delete or hard-delete a user (self or admin only) |
| **Dependencies** | Cascade handling for user's posts, comments, etc. |
| **Acceptance** | Returns `204`, user flagged as deleted |

**Files to modify:**
- `app/repositories/user.py` — if soft-delete not supported, add `deleted_at` field + migration
- `app/services/user_service.py` — add `delete_user()` method with authorization
- `app/api/v1/users.py` — add `DELETE /{user_id}` route
- Alembic migration — if adding `deleted_at` to user model

---

### 3.6 `DELETE /api/v1/photos/profile/{user_id}` — Delete Profile Photo

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Legacy path** | `/api/v1/photos/profile/{userId}` (DELETE) |
| **Effort** | Small (~20 min) |
| **What** | Delete a user's profile photo by user ID |
| **Acceptance** | Returns `204`, photo removed from storage and DB |

**Files to modify:**
- `app/services/photo_service.py` — add `delete_profile_photo()` method
- `app/api/v1/photos.py` — add `DELETE /profile/{user_id}` route

---

## Phase 4 — Cross-Cutting Enhancements

> **Goal:** Idempotency, pagination, documentation, and test coverage.

### 4.1 Split Reactions into POST/DELETE

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | `POST /api/v1/reactions` (toggle — add if not exists, remove if exists) |
| **Target** | `POST /api/v1/reactions` (add reaction, idempotent — returns 409 if exists), `DELETE /api/v1/reactions/{id}` (remove, idempotent) |
| **Effort** | Small (~30 min) |
| **Acceptance** | Reactions use idempotent POST/DELETE instead of toggle |

**Files to modify:**
- `app/services/reaction_service.py` — split toggle into add/remove
- `app/api/v1/reactions.py` — update routes

---

### 4.2 Add Batch Notification Read Endpoint

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **Current** | `PUT /api/v1/notifications/{notification_id}/read` (single only) |
| **Target** | `PUT /api/v1/notifications/read` with `{ "ids": [...] }` or `{ "all": true }` |
| **Effort** | Small (~30 min) |
| **Acceptance** | Can mark multiple notifications as read in one request |

**Files to modify:**
- `app/services/notification_service.py` — add `mark_multiple_as_read()` method
- `app/api/v1/notifications.py` — add batch route

---

### 4.3 Ensure Cursor Pagination on All List Endpoints

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Audit scope** | All `GET` endpoints returning lists |
| **Current** | Some endpoints may be missing pagination or using offset-based |
| **Target** | Consistent cursor-based pagination: `{ data: [...], next_cursor: str, has_next: bool }` |
| **Effort** | Medium (~2 hr audit + fixes) |
| **Acceptance** | Every list endpoint returns paginated response with cursor |

**Files to audit:**
- `app/api/v1/*.py` — check all GET endpoints for pagination
- `app/utils/pagination.py` — cursor helper exists; ensure all list endpoints use it

---

### 4.4 Update OpenAPI Tags and Descriptions

| Item | Detail |
|------|--------|
| **Priority** | 🟢 Low |
| **What** | Ensure all new endpoints have proper `tags`, `summary`, `description` in FastAPI decorators |
| **Effort** | Small (~30 min for all phases) |
| **Acceptance** | `/openapi.json` reflects all new endpoints with clear documentation |

---

### 4.5 Write Tests

| Item | Detail |
|------|--------|
| **Priority** | 🟡 Medium |
| **Scope** | All new endpoints from Phases 1–3 |
| **Type** | Integration tests (using existing `conftest.py` fixtures) |
| **Effort** | Medium (~3–4 hr total) |
| **Acceptance** | Each new endpoint has at least one happy-path + one error test |

**Test files to create/modify:**
- `tests/integration/test_posts.py` — add tests for search + user posts
- `tests/integration/test_comments.py` — add tests for by-id + by-user + latest
- `tests/integration/test_messages.py` — add test for single message
- `tests/integration/test_chats.py` — add test for delete chat
- `tests/integration/test_reactions.py` — add tests for user + content reactions
- `tests/integration/test_users.py` — add test for delete user
- `tests/integration/test_photos.py` — add test for profile photo deletion
- `tests/integration/test_notifications.py` — add tests for type filter + batch read

---

## Implementation Order Summary

```
Phase 1 ──► Phase 3 ──► Phase 4 ──► Phase 2
  (must-      (remaining     (tests,      (restructuring —
   have)       endpoints)     pagination,   moved last to
                              docs)        avoid breaking
                                            existing clients
                                            before full
                                            coverage)
```

| Phase | Estimated total effort |
|-------|----------------------|
| Phase 1 — High-impact missing | ~3.5 hr |
| Phase 2 — RESTful improvements | ~5 hr |
| Phase 3 — Remaining missing | ~2.5 hr |
| Phase 4 — Cross-cutting | ~6.5 hr |
| **Total** | **~17.5 hr** |

---

## How to Use This Plan

Each step should be implemented and committed independently:

```bash
# Example workflow for Step 1.1
# 1. Add repository method
# 2. Add service method
# 3. Add route
# 4. Write tests
# 5. Run full test suite
poetry run pytest
# 6. Commit
git add -A && git commit -m "Add GET /api/v1/posts/user/{user_id} endpoint"
```
