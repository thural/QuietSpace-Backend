# Test Remediation Plan: Final 8 Failures

**Updated:** 2026-07-12
**Status:** 502 tests, 8 failures (96.4% pass rate)

---

## What's Been Completed (Phases 1–4)

| Phase | Root Cause | Fix | Status |
|-------|-----------|-----|--------|
| 1 | `TransactionRequiredException` in *FlowIT (direct repo/entityManager ops outside tx) | Added `@Transactional` to all 10 *FlowIT classes | ✅ |
| 2 | `ConstraintViolationException` from cascade-persisted `ProfileSettings` with null `@NotNull user` | Fixed entity setup + `TestEntityFactory` pattern | ✅ |
| 3 | `IllegalStateException` / `NoSuchBeanDefinitionException` in `@WebMvcTest` slice tests (JwtFilter deps) | Standardized `@WebMvcTest` with `@MockitoBean` for JwtFilter deps | ✅ |
| 4 | Scattered edge cases (mappers, JWT, lazy loading, paging, chat mutation, email service) | Null-safe mappers, JWT fix, paging 0-index, chat copy-on-write, `EmailServiceIT` rewrite | ✅ |
| 4a | Mis-packaged `@WebMvcTest` duplicates in `controller/` vs `controller.slice/` | Removed 4 duplicate files, merged unique assertions into `controller.slice/` | ✅ |
| 4b | AdminFlowIT 400s (4 tests) | Fixed security/validation for admin endpoints | ✅ |
| 4c | NotificationFlowIT 404s (2 tests) | Fixed endpoint path/ID matching | ✅ |
| 4d | AuthFlowIT contract (2 tests) | Duplicate email enforcement + `UserNotFoundException` mapped to 401 | ✅ |
| 4e | ChatFlowIT (wrong status), PhotoFlowIT (upload) | Status alignment + multipart fix | ✅ |

---

## Remaining: 8 Failures

### Group A: WebSocket Handshake — 6 tests (`WebSocketFlowIT`)
**Files:** `src/test/java/dev/thural/quietspace/websocket/WebSocketFlowIT.java:119-365`

**Symptom:** `ExecutionException: jakarta.websocket.DeploymentException: The HTTP response from the server [400] did not permit the HTTP upgrade to WebSocket`

**Root cause:** STOMP endpoint `ws://localhost:{port}/ws` returns HTTP 400 before WebSocket upgrade completes. Likely one of:
- Spring Security filter chain rejecting the upgrade request (no `permitAll()` on `/ws/**`)
- `JwtFilter` attempting to validate the upgrade request and returning 400
- Missing or misconfigured WebSocket endpoint registration in `WebSocketConfig`
- `@Transactional` on the test class may not apply to the WebSocket thread

**Fix steps:**
1. Add explicit logging in `WebSocketConfig` or create a `WebSocketSecurityConfig` to verify endpoint registration
2. Ensure `SecurityFilterChain` permits `/ws/**` without authentication (or configure JwtFilter to skip upgrade requests)
3. Run a single test with `andDo(print())` equivalent to capture the handshake response body
4. Test fix: consider configuring `StandardWebSocketClient` with a custom `HandshakeHandler` that logs the rejection reason
5. If JwtFilter is the culprit, add a path matcher in `JwtFilter` to skip `/ws/**`

---

### Group B: Lazy Serialization — 1 test (`PostFlowIT.getSavedPosts_shouldReturn200`)
**File:** `src/test/java/dev/thural/quietspace/controller/PostFlowIT.java:326-348`

**Symptom:** `ServletException: JpaSystemException: could not serialize` — accessing `user.savedPosts` triggers lazy-load of a collection outside the transaction context during JSON serialization.

**Root cause:** `getSavedPosts` in `PostServiceImpl` returns entities whose lazy associations are not initialized before the transaction closes. The `@Transactional` on the test class keeps the tx open for DB operations but the response serialization happens in Spring MVC's thread, which may not share the same persistence context.

**Fix steps:**
1. Add `@EntityGraph(attributePaths = "savedPosts")` to the repository method `findAllSavedPostsByUserId`
2. OR add `JOIN FETCH p.savedPosts` in the query
3. OR initialize the association explicitly in the service method before mapping to DTO

---

### Group C: Status Code Mismatch — 1 test (`UserFlowIT.getFollowers_shouldReturn200`)
**File:** `src/test/java/dev/thural/quietspace/controller/UserFlowIT.java:139-153`

**Symptom:** Expects HTTP 200, gets 400.

**Root cause:** After `toggleFollow` (which succeeds), the followers query endpoint rejects the request — likely because:
- The `user2Id` is stale (user2 was re-registered on line 142, generating a new ID, but `user2Id` holds the old value)
- Or the endpoint validation fails (e.g., `page-number` param mismatch, or the user cannot view followers of another user)

**Fix steps:**
1. After re-registration on lines 141-142, re-fetch `user1Id` and `user2Id` from the repository
2. OR remove the redundant re-registration (users already registered in `@BeforeEach`)
3. If the issue is param-related, capture the response body to diagnose

---

## Execution Plan

```yaml
step_1_websocket:
  action: Enable WebSocket endpoint logging and identify why upgrade returns 400
  files:
    - src/main/java/dev/thural/quietspace/config/WebSocketConfig.java
  verify: Run WebSocketFlowIT single test

step_2_websocket:
  action: Fix root cause (permitAll on /ws/** or JwtFilter exclusion)
  files:
    - src/main/java/dev/thural/quietspace/config/SecurityConfig.java  # likely
    - src/main/java/dev/thural/quietspace/config/WebSocketConfig.java
    - src/main/java/dev/thural/quietspace/authentication/jwt/JwtFilter.java
  verify: All 6 WebSocketFlowIT tests pass

step_3_lazy_serialization:
  action: Add @EntityGraph to the saved-posts repository query
  files:
    - src/main/java/dev/thural/quietspace/repository/PostRepository.java
    - src/main/java/dev/thural/quietspace/service/impl/PostServiceImpl.java
  verify: PostFlowIT.getSavedPosts_shouldReturn200 passes

step_4_followers_status:
  action: Fix user2Id staleness or endpoint validation in getFollowers test
  files:
    - src/test/java/dev/thural/quietspace/controller/UserFlowIT.java
  verify: UserFlowIT.getFollowers_shouldReturn200 passes

step_5_final:
  action: Run full `./gradlew test`
  verify: 0 failures
```

## Rollback

Each step is a single, revertable commit. If a fix introduces new failures, revert it and diagnose further.
