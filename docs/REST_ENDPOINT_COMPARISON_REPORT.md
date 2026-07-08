# REST Endpoint Comparison Report

**FastAPI (converted) vs Spring Boot (legacy) — Enterprise Social Media Backend Best Practices**

---

## Assessment Criteria

Best practices for 2026 enterprise social media APIs:

| Criterion | Description |
|-----------|-------------|
| **RESTful naming** | Resources as nouns, hierarchical paths, no verbs in URIs |
| **HTTP method semantics** | GET=read, POST=create, PUT=replace, PATCH=partial update, DELETE=remove |
| **Path consistency** | Singular vs plural conventions, uniform param naming (snake_case) |
| **Granularity** | Separate endpoints for distinct operations vs toggle/multi-purpose endpoints |
| **Idempotency** | PUT/DELETE idempotent; POST not; PATCH semantics clear |
| **Security surface** | Principle of least privilege, appropriate auth per endpoint |
| **Error handling** | Structured error responses, proper status codes |
| **API versioning** | Consistent prefix strategy |
| **Discoverability** | Self-documenting paths, OpenAPI 3.1 |

---

## 1. Authentication

### `POST /api/v1/auth/register`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/register` | `/api/v1/auth/register` |
| **Method** | POST | POST |
| **Naming** | ✅ Resource-oriented | ✅ Identical |

**Winner:** Tie — both use the same standard pattern.

**Improvement:** None needed.

---

### `POST /api/v1/auth/login` vs `POST /api/v1/auth/authenticate`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/login` | `/api/v1/auth/authenticate` |
| **Method** | POST | POST |
| **Naming** | ✅ Clear, standard | ❌ Verb "authenticate", not resource-oriented |

**Winner: FastAPI** — `/login` is self-explanatory and matches industry convention (OAuth 2.0 token endpoint patterns). `/authenticate` is overly generic.

**Improvement:** None needed.

---

### `POST /api/v1/auth/refresh` vs `POST /api/v1/auth/refresh-token`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/refresh` | `/api/v1/auth/refresh-token` |
| **Method** | POST | POST |
| **Naming** | ✅ Concise | ✅ Also clear |

**Winner: FastAPI** — Shorter, still unambiguous. Token refresh is implied by the auth context.

**Improvement:** None needed.

---

### `POST /api/v1/auth/logout` vs `POST /api/v1/auth/signout`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/logout` | `/api/v1/auth/signout` |
| **Method** | POST | POST |
| **Naming** | ✅ Industry standard | ❌ "signout" less common |

**Winner: FastAPI** — `/logout` is the universal convention.

**Improvement:** None needed.

---

### `POST /api/v1/auth/activate-account`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/activate-account` | `/api/v1/auth/activate-account` |
| **Method** | POST | POST |
| **Naming** | ✅ Identical | ✅ Identical |

**Winner:** Tie.

**Improvement:** None needed.

---

### `POST /api/v1/auth/resend-code`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/auth/resend-code` | `/api/v1/auth/resend-code` |
| **Method** | POST | POST |
| **Naming** | ✅ Identical | ✅ Identical |

**Winner:** Tie.

**Improvement:** None needed.

---

## 2. Users

### `GET /api/v1/users/search`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/search` | `/api/v1/users/search` |
| **Method** | GET | GET |
| **Naming** | ✅ Resource-oriented | ✅ Identical |
| **Query semantics** | Presumably `?q=` | Presumably `?q=` |

**Winner:** Tie.

**Improvement:** Ensure the response uses cursor-based pagination and returns consistent pagination metadata (`next_cursor`, `has_next`).

---

### `GET /api/v1/users/query`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/query` | `/api/v1/users/query` |
| **Method** | GET | GET |
| **Naming** | ⚠️ Verb in path | ⚠️ Same |

**Winner:** Tie (both use suboptimal naming).

**Improvement:** Merge into `/api/v1/users/search` with additional query parameters (`?firstname=`, `?lastname=`) rather than having two search endpoints. Having both `search` and `query` is confusing.

---

### `GET /api/v1/users/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/{user_id}` | `/api/v1/users/{userId}` |
| **Method** | GET | GET |
| **Param style** | ✅ `snake_case` | ❌ `camelCase` (inconsistent with REST) |

**Winner: FastAPI** — snake_case is consistent with Python ecosystem and the rest of the FastAPI spec. Legacy mixes `camelCase` path params with `kebab-case` paths.

**Improvement:** None needed.

---

### `PATCH /api/v1/users/me` vs `PATCH /api/v1/users`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/me` | `/api/v1/users` |
| **Method** | PATCH | PATCH |
| **Resource targeting** | ✅ Explicit: "update my profile" | ❌ Ambiguous: "update which user?" |

**Winner: FastAPI** — `/users/me` clearly targets the authenticated user's own profile. PATCHing `/users` (the collection) is semantically incorrect in REST — legacy relies on implicit auth context to determine which user to update.

**Improvement:** None needed.

---

### `DELETE /api/v1/users/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/users/{userId}` |
| **Method** | — | DELETE |
| **Need** | High — user self-deletion or admin action | ✅ Present |

**Winner: Legacy** — FastAPI needs this endpoint.

**Improvement:** Add `DELETE /api/v1/users/{user_id}` with authorization checks (self or admin-only). Return `204 No Content`.

---

### `POST /api/v1/users/{id}/follow` vs `POST /api/v1/users/follow/{id}/toggle-follow`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/{user_id}/follow` | `/api/v1/users/follow/{userId}/toggle-follow` |
| **Method** | POST | POST |
| **Idempotency** | ❌ POST non-idempotent for toggle | ❌ "toggle" is inherently non-idempotent |
| **Naming** | ✅ Resource under user | ❌ Verb nesting (`follow/toggle-follow`) |
| **Granularity** | ✅ Separate follow/unfollow via POST/DELETE | ❌ Single toggle endpoint |

**Winner: FastAPI** — REST best practice is to model `follow` as a sub-resource of user: `POST` to create (follow), `DELETE` to remove (unfollow). The legacy "toggle" approach violates REST semantics and makes it impossible to know the user's follow state without an extra query.

**Improvement:** None needed — this is a clean implementation.

---

### `DELETE /api/v1/users/{id}/follow`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Not available (toggle only) |
| **Method** | DELETE | — |
| **Idempotency** | ✅ DELETE is naturally idempotent | ❌ |

**Winner: FastAPI** — Separate unfollow via DELETE is idempotent and RESTful.

**Improvement:** None needed.

---

### `POST /api/v1/users/followers/remove/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/followers/remove/{follower_id}` | `/api/v1/users/followers/remove/{userId}` |
| **Method** | POST | POST |
| **Naming** | ⚠️ Verb "remove" in path | ⚠️ Same |

**Winner:** Tie — both use verb in path.

**Improvement:** Use `DELETE /api/v1/users/{user_id}/followers/{follower_id}` for RESTful consistency. Followers are a sub-resource of the user; removing one is a DELETE on that relationship.

---

### `GET /api/v1/users/{id}/followers`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/{user_id}/followers` | `/api/v1/users/{userId}/followers` |
| **Method** | GET | GET |
| **Naming** | ✅ Consistent | ✅ Consistent |

**Winner:** Tie.

**Improvement:** None needed.

---

### `GET /api/v1/users/{id}/following` vs `GET /api/v1/users/{id}/followings`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/{user_id}/following` | `/api/v1/users/{userId}/followings` |
| **Grammatical correctness** | ✅ "following" (gerund, collection noun) | ❌ "followings" (non-standard plural) |

**Winner: FastAPI** — "following" is the standard term for the set of users a person follows.

**Improvement:** None needed.

---

### `GET /api/v1/users/me` vs `GET /api/v1/users/profile`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/me` | `/api/v1/users/profile` |
| **RESTful** | ✅ `/me` is a well-established convention | ❌ `/profile` introduces a new resource name |

**Winner: FastAPI** — `/users/me` is the industry-standard pattern (GitHub, Slack, etc. all use it). The profile concept should be a sub-resource at `/users/me/profile` if needed, not a standalone collection endpoint.

**Improvement:** None needed.

---

### `PATCH /api/v1/users/me` vs `PATCH /api/v1/users`

(Repeat of earlier — FastAPI wins for semantic clarity)

---

### `GET /api/v1/users/me/settings` + `PUT /api/v1/users/me/settings` vs `PATCH /api/v1/users/profile/settings`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/me/settings` | `/api/v1/users/profile/settings` |
| **Read endpoint** | ✅ GET available | ❌ No GET — read-only not exposed |
| **Write method** | PUT (full replace) | PATCH (partial update) |
| **CRUD completeness** | ✅ Full read/write | ❌ Write-only |

**Winner: FastAPI** — Providing a GET endpoint for settings is critical for UI clients. `PUT` for full settings replacement is also more appropriate than `PATCH` when the entire settings object is being submitted.

**Improvement:** Consider adding `PATCH` as well for partial updates, but PUT is acceptable if the frontend always submits the complete settings object.

---

### `POST /api/v1/users/profile/block/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/users/profile/block/{user_id}` | `/api/v1/users/profile/block/{userId}` |
| **Method** | POST | POST |
| **Naming** | ✅ Consistent | ✅ Consistent |
| **Unblock** | ✅ `DELETE` on same path available | ❌ No unblock endpoint |

**Winner: FastAPI** — Has both block and unblock.

**Improvement:** This should arguably be `POST /api/v1/users/me/block/{user_id}` for consistency with the `/me` pattern. The `profile` nesting is unnecessary.

---

### `DELETE /api/v1/users/profile/block/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |
| **Idempotency** | ✅ DELETE | — |

**Winner: FastAPI** — Provides unblock functionality missing in legacy.

**Improvement:** (Same as above) Consider `DELETE /api/v1/users/me/block/{user_id}` for `/me` consistency.

---

### `GET /api/v1/users/profile/blocked`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |
| **Path** | `/api/v1/users/profile/blocked` | — |

**Winner: FastAPI** — Essential feature for privacy control.

**Improvement:** Consider `GET /api/v1/users/me/blocked` for consistency.

---

### `GET /api/v1/users/online`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing (only via WebSocket) |

**Winner: FastAPI** — REST endpoint for online users provides simpler client integration without requiring a WebSocket connection.

**Improvement:** Ensure this supports pagination for large user bases. Consider adding `?user_ids=...` query parameter for batch status lookup.

---

### `GET /api/v1/users/{id}/save`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |
| **Path** | `/api/v1/users/{user_id}/save` | — |
| **Purpose** | Get user with relations | — |

**Winner: FastAPI** — Useful for eager-loading user profiles with related data.

**Improvement:** The path name `/save` is confusing (sounds like a save action rather than "user with saved relations"). Consider renaming to `/api/v1/users/{user_id}/profile` or `/api/v1/users/{user_id}/detail`.

---

## 3. Posts

### `GET /api/v1/posts`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts` | `/api/v1/posts` |
| **Method** | GET | GET |
| **Naming** | ✅ Identical | ✅ Identical |

**Winner:** Tie.

**Improvement:** Ensure cursor-based pagination with `next_cursor` and `has_next` fields.

---

### `POST /api/v1/posts`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts` | `/api/v1/posts` |
| **Method** | POST | POST |
| **Naming** | ✅ Identical | ✅ Identical |

**Winner:** Tie.

**Improvement:** Consider supporting multipart upload for media attachments in the same request, or provide a separate media attachment endpoint.

---

### `GET /api/v1/posts/search`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/posts/search` |

**Winner: Legacy** — Essential feature for any social media platform.

**Improvement (FastAPI):** Add `GET /api/v1/posts/search?q={query}` with full-text search support (PostgreSQL `tsvector` or Meilisearch/Elasticsearch integration). Include pagination and relevance sorting.

---

### `GET /api/v1/posts/user/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/posts/user/{userId}` |

**Winner: Legacy** — Critical for user profile pages.

**Improvement (FastAPI):** Add `GET /api/v1/posts/user/{user_id}` with pagination. This is a must-have for displaying user profiles.

---

### `GET /api/v1/posts/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/{post_id}` | `/api/v1/posts/{postId}` |
| **Method** | GET | GET |
| **Naming** | ✅ Consistent | ⚠️ camelCase |

**Winner:** FastAPI (snake_case consistency).

**Improvement:** None needed.

---

### `PATCH /api/v1/posts/{id}` vs `PUT /api/v1/posts/{id}` + `PATCH /api/v1/posts/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Full update (PUT)** | ❌ Not available | ✅ Available |
| **Partial update (PATCH)** | ✅ Available | ✅ Available |
| **Redundancy** | ✅ Single method | ❌ Both PUT and PATCH on same resource |

**Winner: FastAPI** — Having both `PUT` and `PATCH` on the same resource is redundant and confusing. REST best practice is `PATCH` for partial updates unless full replacement semantics are truly needed. FastAPI picks one and keeps it simple.

**Improvement:** None needed.

---

### `DELETE /api/v1/posts/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/{post_id}` | `/api/v1/posts/{postId}` |
| **Method** | DELETE | DELETE |

**Winner:** Tie.

**Improvement:** Ensure soft-delete is supported (the legacy has a `deleted_at` pattern on messages; check if posts do too).

---

### `POST /api/v1/posts/repost`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/repost` | `/api/v1/posts/repost` |
| **Method** | POST | POST |
| **Naming** | ✅ Identical | ✅ Identical |

**Winner:** Tie.

**Improvement:** Consider `POST /api/v1/posts/{post_id}/repost` for a more RESTful "create a repost under this post" pattern.

---

### `POST /api/v1/posts/vote-poll`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/vote-poll` | `/api/v1/posts/vote-poll` |
| **Method** | POST | POST |

**Winner:** Tie.

**Improvement:** Consider `POST /api/v1/polls/{poll_id}/vote` for better resource orientation. Polls could be a sub-resource of posts: `POST /api/v1/posts/{post_id}/polls/{poll_id}/vote`.

---

### `POST|DELETE /api/v1/posts/{id}/save` vs `PATCH /api/v1/posts/saved/{id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Save** | `POST /posts/{post_id}/save` | `PATCH /posts/saved/{postId}` |
| **Unsave** | `DELETE /posts/{post_id}/save` | ❌ (toggle via PATCH) |
| **Idempotency** | ✅ POST/DELETE clear semantics | ❌ PATCH for toggle |

**Winner: FastAPI** — Clean RESTful save/unsave using POST/DELETE.

**Improvement:** None needed.

---

### `GET /api/v1/posts/saved`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/saved` | `/api/v1/posts/saved` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** None needed.

---

### `GET /api/v1/posts/commented/{user_id}` vs `GET /api/v1/posts/user/{userId}/commented`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/posts/commented/{user_id}` | `/api/v1/posts/user/{userId}/commented` |
| **RESTful hierarchy** | ❌ Flattened (`commented` as collection) | ✅ Better: posts/user/{id}/commented |

**Winner: Legacy** — The path `posts/user/{userId}/commented` reads as "posts by user X that they commented on", which follows proper resource nesting. FastAPI's `posts/commented/{userId}` implies `commented` is a sub-resource of `posts`, which is awkward.

**Improvement:** Adopt legacy's path structure: `GET /api/v1/posts/user/{user_id}/commented`.

---

## 4. Comments

### `POST /api/v1/comments`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/comments` | `/api/v1/comments` |
| **Method** | POST | POST |

**Winner:** Tie.

**Improvement:** Consider `POST /api/v1/posts/{post_id}/comments` for better RESTful nesting. Comments are a sub-resource of posts.

---

### `GET /api/v1/comments/post/{post_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/comments/post/{post_id}` | `/api/v1/comments/post/{postId}` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** Consider `GET /api/v1/posts/{post_id}/comments` for proper resource nesting.

---

### `GET /api/v1/comments/{comment_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/comments/{commentId}` |

**Winner: Legacy** — Getting a single comment by ID is needed for comment permalinks and detail views.

**Improvement (FastAPI):** Add `GET /api/v1/comments/{comment_id}` — essential for deep-linking to specific comments.

---

### `PATCH /api/v1/comments/{comment_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/comments/{comment_id}` | `/api/v1/comments/{commentId}` (PATCH) |
| **Additional methods** | PATCH only | PUT + PATCH + DELETE |

**Winner:** FastAPI (single HTTP method, no redundancy).

**Improvement:** None needed.

---

### `DELETE /api/v1/comments/{comment_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/comments/{comment_id}` | `/api/v1/comments/{commentId}` |

**Winner:** Tie.

**Improvement:** None needed.

---

### `GET /api/v1/comments/{comment_id}/replies`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/comments/{comment_id}/replies` | `/api/v1/comments/{commentId}/replies` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** None needed.

---

### `GET /api/v1/comments/user/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/comments/user/{userId}` |

**Winner: Legacy** — Needed for user profile comment history.

**Improvement (FastAPI):** Add `GET /api/v1/comments/user/{user_id}`.

---

### `GET /api/v1/comments/user/{userId}/post/{postId}/latest`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Useful optimization for displaying "your latest comment" on a post.

**Improvement (FastAPI):** Add `GET /api/v1/comments/user/{user_id}/post/{post_id}/latest` if the frontend requires it, or handle this via a query parameter on the existing comment list endpoint (e.g., `?user_id=X&limit=1`).

---

## 5. Chats

### `GET /api/v1/chats`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |
| **Path** | `/api/v1/chats` | — |

**Winner: FastAPI** — Listing chats is essential for the chat UI sidebar.

**Improvement:** None needed.

---

### `POST /api/v1/chats`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/chats` | `/api/v1/chats` |
| **Method** | POST | POST |

**Winner:** Tie.

**Improvement:** Consider whether chat creation should be idempotent (return existing 1:1 chat between two users if it already exists).

---

### `GET /api/v1/chats/{chat_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/chats/{chat_id}` | `/api/v1/chats/{chatId}` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** None needed.

---

### `PATCH /api/v1/chats/{chat_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present (PATCH) | ❌ Missing |

**Winner: FastAPI** — Supports updating chat metadata (name, avatar, etc.).

**Improvement:** None needed.

---

### `DELETE /api/v1/chats/{chat_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/chats/{chatId}` |

**Winner: Legacy** — Chat deletion is needed for cleanup.

**Improvement (FastAPI):** Add `DELETE /api/v1/chats/{chat_id}` with soft-delete support.

---

### `GET /api/v1/chats/members/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ `/api/v1/chats/members/{userId}` |

**Winner: Legacy** — Needed to list all chats a user belongs to (alternative to the generic chat list which might include chats the user isn't in).

**Improvement (FastAPI):** If `GET /api/v1/chats` already filters to the current user's chats (which it should), this may not be needed. Add only if `GET /api/v1/chats` returns all chats.

---

### `POST /api/v1/chats/{chat_id}/participants` vs `PATCH /api/v1/chats/{chatId}/members/add/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/chats/{chat_id}/participants` | `/api/v1/chats/{chatId}/members/add/{userId}` |
| **Method** | POST | PATCH |
| **RESTful** | ✅ POST on sub-resource collection | ❌ PATCH + verb in path ("add") |

**Winner: FastAPI** — POST on `/participants` to add a participant is cleaner. The legacy method PATCH + `/add/` violates REST conventions.

**Improvement:** Ideally the request body or path param should specify which user(s) to add. Consider `POST /api/v1/chats/{chat_id}/participants` with `{ "user_id": "..." }` in the body.

---

### `DELETE /api/v1/chats/{chat_id}/participants/{user_id}` vs `PATCH /api/v1/chats/{chatId}/members/remove/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/chats/{chat_id}/participants/{user_id}` | `/api/v1/chats/{chatId}/members/remove/{userId}` |
| **Method** | DELETE | PATCH |
| **RESTful** | ✅ DELETE on specific sub-resource | ❌ PATCH + verb in path ("remove") |

**Winner: FastAPI** — Clean RESTful DELETE on the participant sub-resource.

**Improvement:** None needed.

---

## 6. Messages

### `POST /api/v1/messages`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/messages` | `/api/v1/messages` |
| **Method** | POST | POST |

**Winner:** Tie.

**Improvement:** Consider `POST /api/v1/chats/{chat_id}/messages` for RESTful nesting.

---

### `GET /api/v1/messages/chat/{chat_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/messages/chat/{chat_id}` | `/api/v1/messages/chat/{chatId}` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** Consider `GET /api/v1/chats/{chat_id}/messages` for RESTful nesting. Also ensure cursor-based pagination.

---

### `DELETE /api/v1/messages/{message_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/messages/{message_id}` | `/api/v1/messages/{messageId}` |
| **Method** | DELETE | DELETE |

**Winner:** Tie.

**Improvement:** Soft-delete is already supported (the Message model has `deleted_at`).

---

### `GET /api/v1/messages/chat/{chatId}/message/{messageId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Single message retrieval is needed for permalinks.

**Improvement (FastAPI):** Add `GET /api/v1/messages/{message_id}` (simpler path than legacy's nested version).

---

### `GET /api/v1/messages/unread`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — Useful REST endpoint for showing unread message count without WebSocket.

**Improvement:** Ensure this returns paginated results or a count, not both. Consider separating into `/api/v1/messages/unread/count`.

---

### `PUT /api/v1/messages/{message_id}/read`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ (handled via WebSocket only) |

**Winner: FastAPI** — REST endpoint for read receipts provides a simpler alternative to WebSocket-only in legacy.

**Improvement:** None needed.

---

## 7. Notifications

### `GET /api/v1/notifications`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/notifications` | `/api/v1/notifications` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** Ensure cursor-based pagination.

---

### `GET /api/v1/notifications/unread/count` vs `GET /api/v1/notifications/count-pending`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/notifications/unread/count` | `/api/v1/notifications/count-pending` |
| **Naming** | ✅ Descriptive (`unread` is standard term) | ❌ `pending` is ambiguous |

**Winner: FastAPI** — More descriptive path. `unread` is the industry standard term.

**Improvement:** None needed.

---

### `PUT /api/v1/notifications/{notification_id}/read` vs `POST /api/v1/notifications/seen/{contentId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/notifications/{notification_id}/read` | `/api/v1/notifications/seen/{contentId}` |
| **Granularity** | ✅ Mark individual notification as read | ⚠️ Mark all notifications for a content item as seen |
| **Method** | PUT | POST |

**Winner: FastAPI** — Marking by individual notification ID is more granular and precise. The legacy approach of marking by content ID (e.g., "mark all notifications about post X as seen") loses individual notification tracking.

**Improvement:** Consider also supporting a batch endpoint: `PUT /api/v1/notifications/read` with `{ "ids": [...] }` in the body.

---

### `GET /api/v1/notifications/type/{notificationType}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Filtering by type is needed for notification preference management.

**Improvement (FastAPI):** Add `GET /api/v1/notifications?type={type}` as a query parameter instead of a path parameter. More flexible and follows filtering conventions.

---

### `POST /api/v1/notifications/process` + `/process-reaction`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoints** | ❌ Missing | ✅ Present (2 internal endpoints) |

**Winner:** FastAPI — These are legacy internal endpoints that should be handled by the service layer, not exposed as API. FastAPI's service-oriented architecture handles this better internally.

**Improvement:** None needed (these should NOT be added to FastAPI).

---

## 8. Reactions

### `POST /api/v1/reactions` vs `POST /api/v1/reactions/toggle-reaction`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/reactions` | `/api/v1/reactions/toggle-reaction` |
| **Method** | POST | POST |
| **Naming** | ✅ Concise, resource-oriented | ❌ Verb in path |

**Winner: FastAPI** — Clearer path. POST on the collection implies creation.

**Improvement:** Like follow/unfollow, consider splitting into `POST /api/v1/reactions` (add) and `DELETE /api/v1/reactions/{id}` (remove) for idempotency.

---

### `GET /api/v1/reactions/count/{post_id}` vs `GET /api/v1/reactions/count`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/reactions/count/{post_id}` | `/api/v1/reactions/count` (query params) |
| **Restful** | ✅ Post ID in path | ❌ No path resource — just `/count` with query params |

**Winner: FastAPI** — Path-based resource identification is more RESTful.

**Improvement:** Consider nesting under posts: `GET /api/v1/posts/{post_id}/reactions/count`.

---

### `GET /api/v1/reactions/post/{post_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — Dedicated endpoint for post reactions is useful.

**Improvement:** Consider `GET /api/v1/posts/{post_id}/reactions` for RESTful nesting.

---

### `GET /api/v1/reactions/user`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Getting reactions by the current user is needed.

**Improvement (FastAPI):** Add `GET /api/v1/users/me/reactions` or `GET /api/v1/reactions?user_id={user_id}`.

---

### `GET /api/v1/reactions/content`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Getting reactions for specific content is needed.

**Improvement (FastAPI):** Likely same as `GET /api/v1/reactions/post/{post_id}`. If content types other than posts exist, use `GET /api/v1/reactions?content_type=post&content_id={id}`.

---

## 9. Photos

### `POST /api/v1/photos/profile`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/photos/profile` | `/api/v1/photos/profile` |
| **Method** | POST | POST |

**Winner:** Tie.

**Improvement:** Consider `POST /api/v1/users/me/profile/photo` for RESTful nesting.

---

### `GET /api/v1/photos/{filename}` vs `GET /api/v1/photos/{name}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/photos/{filename}` | `/api/v1/photos/{name}` |
| **Param name** | ✅ `filename` (descriptive) | ⚠️ `name` (ambiguous) |

**Winner: FastAPI** — `filename` is more descriptive.

**Improvement:** None needed.

---

### `DELETE /api/v1/photos/profile/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ **Missing** | ✅ Present |

**Winner: Legacy** — Profile photo deletion is needed.

**Improvement (FastAPI):** Add `DELETE /api/v1/users/{user_id}/profile/photo` or `DELETE /api/v1/photos/{photo_id}` if photo ID is known.

---

### `POST /api/v1/photos`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — Generic photo upload endpoint.

**Improvement:** None needed.

---

### `GET /api/v1/photos/post/{post_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — Post photo retrieval is useful.

**Improvement:** Consider `GET /api/v1/posts/{post_id}/photos`.

---

### `DELETE /api/v1/photos/{photo_id}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — Generic photo deletion by ID.

**Improvement:** None needed.

---

## 10. Admin

### `GET /api/v1/admin/users`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/admin/users` | `/api/v1/admin/users` |
| **Method** | GET | GET |

**Winner:** Tie.

**Improvement:** Ensure pagination and filtering (by role, status, etc.).

---

### `DELETE /api/v1/admin/users/{user_id}` vs `POST /api/v1/admin/{userId}`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Path** | `/api/v1/admin/users/{user_id}` | `/api/v1/admin/{userId}` |
| **Method** | DELETE | POST |
| **RESTful** | ✅ DELETE for deletion | ❌ POST for deletion (abuse of method) |

**Winner: FastAPI** — Using DELETE for deletion is semantically correct. Legacy POSTs to an `/{id}` resource which is both non-standard and non-idempotent.

**Improvement:** None needed.

---

### `PUT /api/v1/admin/users/{user_id}/disable`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing |

**Winner: FastAPI** — User disable is an important admin feature.

**Improvement:** Consider splitting into `PATCH /api/v1/admin/users/{user_id}` with `{ "status": "disabled" }` in the body for a more general admin user update endpoint.

---

### `GET /api/v1/admin`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ❌ Missing | ✅ Present (hello admin) |

**Winner:** Neither — this is a trivial health/debug endpoint.

**Improvement (FastAPI):** Not needed. The `/health` endpoint serves this purpose better.

---

## 11. Health / Debug

### `GET /health`

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ Present | ❌ Missing (uses `/hello` instead) |
| **Depth** | ✅ Checks DB + Redis status | ❌ Returns static string |

**Winner: FastAPI** — `/health` with component-level checks (DB, Redis) is superior to a static string response.

**Improvement:** None needed.

---

## Cross-Cutting Improvements for FastAPI

### 1. Resource Nesting

Several endpoints would benefit from proper RESTful nesting:

| Current FastAPI | Suggested |
|----------------|-----------|
| `POST /api/v1/comments` | `POST /api/v1/posts/{post_id}/comments` |
| `GET /api/v1/comments/post/{post_id}` | `GET /api/v1/posts/{post_id}/comments` |
| `POST /api/v1/messages` | `POST /api/v1/chats/{chat_id}/messages` |
| `GET /api/v1/messages/chat/{chat_id}` | `GET /api/v1/chats/{chat_id}/messages` |
| `POST /api/v1/photos/profile` | `POST /api/v1/users/me/profile/photo` |
| `GET /api/v1/reactions/count/{post_id}` | `GET /api/v1/posts/{post_id}/reactions/count` |
| `GET /api/v1/reactions/post/{post_id}` | `GET /api/v1/posts/{post_id}/reactions` |

### 2. Missing Endpoints (Priority Order)

| Priority | Endpoint | Reason |
|----------|----------|--------|
| 🔴 High | `GET /api/v1/posts/user/{user_id}` | User profile post history |
| 🔴 High | `GET /api/v1/posts/search?q=...` | Content discovery |
| 🟡 Medium | `GET /api/v1/comments/{comment_id}` | Comment permalink |
| 🟡 Medium | `GET /api/v1/comments/user/{user_id}` | User comment history |
| 🟡 Medium | `GET /api/v1/reactions/user` | User reaction history |
| 🟡 Medium | `GET /api/v1/notifications?type=...` | Notification filtering |
| 🟢 Low | `DELETE /api/v1/chats/{chat_id}` | Chat deletion |
| 🟢 Low | `GET /api/v1/messages/{message_id}` | Message permalink |
| 🟢 Low | `DELETE /api/v1/users/{user_id}` | User deletion |

### 3. Idempotency Improvements

- **Reactions**: Split `POST /api/v1/reactions` into `POST` (add) + `DELETE /api/v1/reactions/{id}` (remove) rather than toggle.
- **Notifications read**: Add batch endpoint `PUT /api/v1/notifications/read` with `{ "ids": [...] }` in the body.

### 4. Path Consistency

- Use `/me` consistently: `/api/v1/users/me/block/{id}` instead of `/api/v1/users/profile/block/{id}`.
- Use snake_case for all path parameters for consistency (already done in FastAPI).

---

## Summary: FastAPI Strengths & Weaknesses

| Aspect | Verdict |
|--------|---------|
| **HTTP method semantics** | ✅ Excellent — uses DELETE, PATCH, POST correctly |
| **Path naming** | ✅ Clean snake_case, resource-oriented |
| **Granularity** | ✅ Prefers separate endpoints over toggle/overloaded endpoints |
| **Coverage** | ⚠️ ~73% of legacy endpoints implemented |
| **Resource nesting** | ❌ Some endpoints should be nested under parent resources |
| **New features** | ✅ 15+ endpoints not in legacy (health, online, block management, photo CRUD) |
| **Overall** | **Stronger architecture than legacy** — cleaner REST, better HTTP method use, more granular endpoints. Main gap is missing ~17 endpoints, particularly around post/comment retrieval and reaction querying. |
