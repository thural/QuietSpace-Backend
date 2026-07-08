# Deprecated REST Endpoints

**Status:** Deprecated — responding with `Deprecation: true` header; planned for removal in a future release.

---

## 1. `GET /api/v1/users/query`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `GET /api/v1/users/search` |
| **Why** | Verb in path (`query`); duplicates the search endpoint with a non-standard name. Both do advanced user search with different parameter shapes, confusing consumers. |
| **Removal risk** | Low — frontend should use `/search` with `?username=`, `?firstname=`, `?lastname=` params instead. |

---

## 2. `POST /api/v1/users/followers/remove/{follower_id}`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `DELETE /api/v1/users/{user_id}/followers/{follower_id}` |
| **Why** | Verb in path (`remove`); uses POST for a deletion operation. The canonical path uses DELETE with proper resource nesting. |
| **Removal risk** | Medium — mobile clients may hardcode this path. The canonical version requires the `user_id` in the path (self-only), while the old path only takes `follower_id`. |

---

## 3. `GET /api/v1/users/{user_id}/save`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `GET /api/v1/users/{user_id}/detail` |
| **Why** | Misleading resource name (`save` reads like a verb/action). The endpoint fetches a user with eagerly loaded relations (posts, comments) — `/detail` better describes this. |
| **Removal risk** | Low — rename in frontend is trivial. |

---

## 4. `POST /api/v1/users/profile/block/{user_id}`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `POST /api/v1/users/me/block/{user_id}` |
| **Why** | Unnecessary `/profile/` nesting; the `/me/` convention is the established standard for current-user operations (GitHub, Slack, etc.). |
| **Removal risk** | Low — same operation, different path. |

---

## 5. `DELETE /api/v1/users/profile/block/{user_id}`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `DELETE /api/v1/users/me/block/{user_id}` |
| **Why** | Same nesting issue as #4. |
| **Removal risk** | Low. |

---

## 6. `GET /api/v1/users/profile/blocked`

| Item | Detail |
|------|--------|
| **Deprecated in favour of** | `GET /api/v1/users/me/blocked` |
| **Why** | Same nesting issue as #4. |
| **Removal risk** | Low. |

---

## Migration Guide

Clients using the deprecated paths should switch to the canonical alternatives above. The deprecated endpoints continue to function identically — only the path changes. Best practice is to prefer the `/me/` prefix for all operations scoped to the authenticated user.
