# Test Remediation — Final Failure Analysis Report

**Date:** 2026-07-11
**Plan:** `docs/plans/test-remediation-v2.md`
**Result:** 513 total tests → **18 remaining failures** (down from 135). Reduction: **86.7%**.

---

## Summary

The four phases of the remediation plan were executed. Systemic root causes were eliminated;
the residual 18 failures are isolated edge cases (per-test status-code/serialization mismatches
and a WebSocket handshake issue) that each require individual diagnosis.

| Phase | Root cause | Scope | Outcome |
|-------|-----------|-------|---------|
| 1 | `TransactionRequiredException` in `*FlowIT` (direct `repository`/`entityManager` ops outside a transaction) | 11 classes / 80 tests | ✅ Resolved — added `@Transactional` to all `*FlowIT` classes |
| 2 | `ConstraintViolationException` — cascade-persisted default `ProfileSettings` had a null `@NotNull user` | 8 repo test classes / 39 tests | ✅ Resolved — `User.ensureProfileSettings()` lifecycle callback |
| 3 | `IllegalStateException` — `@WebMvcTest` loads `JwtFilter` without its mocked deps | 3 controller tests / 12 tests | ✅ Resolved — added `@MockitoBean` for `TokenRepository`/`JwtService`/`UserDetailsService` + `@AutoConfigureMockMvc(addFilters = false)` |
| 4 | Scattered edge cases (mapper null-safety, JWT handling, lazy loading, paging) | ~28 tests | ⚠️ Partially resolved (see below); 18 remain |

### Fixes applied in Phase 4 (committed)
- `UserMapper`: `isFollowing` now uses `getFollowings()`; null-safe `profileSettings`/`followers`/`followings`.
- `PagingProvider`: reverted an incorrect 1-indexed change; kept 0-indexed convention (verified by `PagingProviderTest`).
- `UserServiceImpl` / `PostServiceImpl`: `@Transactional` added to read methods that access lazy collections (app uses `open-in-view: false`).
- `JwtFilter`: catches `io.jsonwebtoken.JwtException` and continues unauthenticated.
- `GlobalExceptionHandler`: maps `JwtException` → `401 UNAUTHORIZED`.
- `ChatServiceImpl`: copies `getUsers()` into a mutable `ArrayList` before mutating.
- `UserServiceIT`: asserts page 0; `UserMapperTest`: corrected `isFollowing` expectation coupled to the old bug.

---

## Remaining 18 Failures (Edge Cases)

### A. WebSocket handshake — 6 tests (`WebSocketFlowIT`)
**Symptom:** `ExecutionException: jakarta.websocket.DeploymentException: The HTTP response from the server [400] did not permit the HTTP upgrade to WebSocket` (all 6 tests).
**Root cause:** The STOMP/WebSocket handshake to `ws://localhost:{port}/ws` is rejected with **HTTP 400** before the upgrade. This is *not* the prior `TransactionRequiredException` (resolved by Phase 1). The 400 originates in the handshake handling — most likely Spring Security / `JwtFilter` rejecting the upgrade request, or a mismatch in the `WebSocketMessageBrokerConfigurer` handshake configuration. Because the WebSocket request runs on a separate thread, it does not share the test's `@Transactional` context (expected).
**Recommended fix:** Verify the `/ws` endpoint's handshake interceptor and security rules; ensure the upgrade request is permitted (and that the auth token is validated without returning 400). Confirm `WebSocketConfig` registers the STOMP endpoint and that `JwtFilter`/security does not abort the upgrade.

### B. Status-code mismatches in controller FlowIT — 11 tests
| Test | Expected | Actual | Likely cause |
|------|----------|--------|--------------|
| `AdminFlowIT.sayHello_shouldReturn200` | 200 | 400 | Admin endpoints reject request (validation / auth on unprotected call) |
| `AdminFlowIT.listUsers_shouldReturn200` | 200 | 400 | Same |
| `AdminFlowIT.sayHello_withoutAdminRole_shouldReturn403` | 403 | 400 | Security filter returns 400 instead of 403 |
| `AdminFlowIT.deleteUser_shouldReturn204` | 204 | 400 | Same |
| `NotificationFlowIT.markNotificationAsSeen_shouldReturn202` | 202 | 404 | Endpoint requires existing notification id / path mismatch |
| `NotificationFlowIT.processNotification_shouldReturn200` | 200 | 404 | Same |
| `ChatFlowIT.removeMember_fromChat_shouldReturn200` | 200 | 204 | Controller returns `noContent()`; test expects 200 + body |
| `PhotoFlowIT.uploadProfilePhoto_shouldReturn201` | 201 | 400 | Multipart/file validation rejects the upload |
| `UserFlowIT.getFollowers_shouldReturn200` | 200 | 400 | Followers query returns 400 (validation/access) |
| `AuthFlowIT.register_givenExistingEmail_shouldReturn400` | 400 | 200 | Registration does not enforce duplicate-email uniqueness |
| `AuthFlowIT.authenticate_givenUnknownEmail_shouldReturn401` | 401 | 404 | `UserNotFoundException` handler returns 404; auth should return 401 |

**Root cause (B):** These are genuine test-vs-implementation contract gaps. Some are missing server-side validation (duplicate email), some are handler semantics (`UserNotFoundException` → 404 vs 401 for auth), some are controller response-shape choices (204 vs 200), and some are request validation (photo upload, admin endpoints). Each should be reconciled individually — prefer fixing the implementation where the test reflects the intended API contract, and adjust the test where the implementation is correct.

### C. Lazy serialization — 1 test (`PostFlowIT.getSavedPosts_shouldReturn200`)
**Symptom:** `ServletException: JpaSystemException: could not serialize`.
**Root cause:** Even with `@Transactional` on `getSavedPostsByUser`, the `PostResponse` mapping touches a lazy collection/association that is not initialized within the transaction (the `could not serialize` occurs during response serialization). Requires `@EntityGraph`/`JOIN FETCH` or initializing the needed association in the service before mapping.

---

## Recommended Next Steps
1. **WebSocket handshake (A):** diagnose `/ws` upgrade 400 in `WebSocketConfig` + security; this is the largest single remaining block (6 tests).
2. **Controller contract tests (B):** triage each status-code mismatch — fix server-side where the test is correct, adjust the test where the server is correct.
3. **Lazy serialization (C):** add fetch joining / initialize associations in `PostServiceImpl.getSavedPostsByUser`.

After A/B/C are addressed, the suite is expected to reach **0 failures**.
