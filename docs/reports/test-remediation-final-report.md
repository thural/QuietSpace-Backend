# Test Remediation — Final Failure Analysis Report (Refreshed 2026-07-12)

**Suite run:** 2026-07-12 08:33 UTC  
**Result:** 502 total tests → **18 failures** (down from 135 on 2026-07-11).  
**Pass rate:** 96.4%

---

## Executive Summary

The 4-phase remediation plan from `docs/plans/test-remediation-v2.md` has been fully executed.
Systemic root causes (transaction management, bean wiring, validation, mapper null-safety, JWT handling) were eliminated. The residual 18 failures are **isolated edge cases** — per-test status-code mismatches and a WebSocket handshake issue — each requiring individual diagnosis.

| Phase | Root cause | Scope | Outcome |
|-------|-----------|-------|---------|
| 1 | `TransactionRequiredException` in `*FlowIT` (direct `repository`/`entityManager` ops outside a tx) | 11 classes / 80 tests | ✅ Resolved |
| 2 | `ConstraintViolationException` — cascade-persisted default `ProfileSettings` had null `@NotNull user` | 8 repo test classes / 39 tests | ✅ Resolved |
| 3 | `IllegalStateException` — `@WebMvcTest` loads `JwtFilter` without its mocked deps | 3 controller tests / 12 tests | ✅ Resolved |
| 4 | Scattered edge cases (mapper, JWT, lazy loading, paging, chat mutation) | ~28 tests | ⚠️ Partially resolved (see below) |
| Struct. | Mis-packaged `@WebMvcTest` duplicates in `controller/` vs `controller.slice/` | 4 duplicate files | ✅ Resolved |

### Non-test hygiene completed in this session
- `controller/` package cleanup: removed `AdminControllerTest`, `NotificationControllerTest`, `ReactionControllerTest` duplicates; merged their unique assertions into `controller.slice/` versions per `integration-test-guidelines.md` §6.
- Guidelines doc (`integration-test-guidelines.md` §4) updated: the §4 Full-Context IT template now includes `@Transactional` and explains when it's required.

---

## Remaining 18 Failures (fresh, authoritative)

### A. WebSocket handshake — 6 tests (`WebSocketFlowIT`)
**Symptom:** `ExecutionException: jakarta.websocket.DeploymentException: The HTTP response from the server [400] did not permit the HTTP upgrade to WebSocket`  
**Root cause:** The STOMP/WebSocket handshake to `ws://localhost:{port}/ws` is rejected with HTTP 400 before upgrade. This is *not* a transaction issue. Likely cause: Spring Security / `JwtFilter` rejecting the upgrade request, or a mismatch in the `WebSocketMessageBrokerConfigurer` handshake configuration. Because the WebSocket request runs on a separate thread, it does not share the test's `@Transactional` context.  
**Recommended fix:** Verify `WebSocketConfig` registers the STOMP endpoint correctly; confirm the handshake interceptor permits the upgrade and that `JwtFilter` validation does not return 400 on the upgrade request itself.

### B. Status-code mismatches in controller FlowIT — 11 tests
| Test | Expected | Actual | Likely cause |
|------|----------|--------|--------------|
| `AdminFlowIT.sayHello_shouldReturn200` | 200 | 400 | Valid admin JWT still rejected — server-side auth/validation gap |
| `AdminFlowIT.listUsers_shouldReturn200` | 200 | 400 | Same |
| `AdminFlowIT.sayHello_withoutAdminRole_shouldReturn403` | 403 | 400 | Security filter returns 400 instead of 403 |
| `AdminFlowIT.deleteUser_shouldReturn204` | 204 | 400 | Same |
| `NotificationFlowIT.markNotificationAsSeen_shouldReturn202` | 202 | 404 | Endpoint requires existing notification ID / path mismatch |
| `NotificationFlowIT.processNotification_shouldReturn200` | 200 | 404 | Same |
| `ChatFlowIT.removeMember_fromChat_shouldReturn200` | 200 | 204 | Controller returns `noContent()`; test expects 200 + body |
| `PhotoFlowIT.uploadProfilePhoto_shouldReturn201` | 201 | 400 | Multipart/file validation rejects the upload |
| `UserFlowIT.getFollowers_shouldReturn200` | 200 | 400 | Followers query returns 400 (validation/access) |
| `AuthFlowIT.register_givenExistingEmail_shouldReturn400` | 400 | 200 | Registration does not enforce duplicate-email uniqueness |
| `AuthFlowIT.authenticate_givenUnknownEmail_shouldReturn401` | 401 | 404 | `UserNotFoundException` handler returns 404; auth should return 401 |

**Root cause (B):** Genuine test-vs-implementation contract gaps. Some are missing server-side validation (duplicate email), some are handler semantics (`UserNotFoundException` → 404 vs 401 for auth), some are controller response-shape choices (204 vs 200), some are request validation (photo upload, admin endpoints).

### C. Lazy serialization — 1 test (`PostFlowIT.getSavedPosts_shouldReturn200`)
**Symptom:** `ServletException: JpaSystemException: could not serialize`  
**Root cause:** Even with `@Transactional` on `getSavedPostsByUser`, the `PostResponse` mapping touches a lazy collection/association not initialized within the transaction. Requires `@EntityGraph`/`JOIN FETCH` or initializing the needed association in the service before mapping.

---

## Recommended Next Steps (priority order)

1. **AdminFlowIT 400s (4 tests, ~1–2h)** — Capture the actual response body by running a single test with a temporary `andReturn()` assertion. Most likely: a shared `HandlerMethodArgumentResolver` or validation issue that affects all admin endpoints. Fixing this one cause resolves 4 tests at once.
2. **WebSocket handshake 400 (6 tests, ~1h)** — Add explicit logging in `WebSocketConfig` / `WebSocketSecurityConfig` to see why the upgrade returns 400. Often a missing `permitAll()` on `/ws/**` or a failing `HandshakeHandler`.
3. **AuthFlowIT contract mismatches (2 tests, 30 min)** — `authenticate` unknown-email: change `UserNotFoundException` mapping to 401 inside `AuthController` or the global handler when triggered from auth endpoints. `register` duplicate-email: add a uniqueness check in `RegistrationService`.
4. **NotificationFlowIT 404s (2 tests, 30 min)** — Verify the notification endpoints' path variables / IDs; most likely a `PathVariable` mismatch or missing `@NotificationId` converter.
5. **PostFlowIT serialization (1 test, 30 min)** — Add `@EntityGraph` or initialize associations in `getSavedPostsByUser`.
6. **ChatFlowIT / UserFlowIT / PhotoFlowIT single mismatches (3 tests)** — Align test expectations with actual controller response codes/validation (either fix the controller or update the test).

After A–F: expected 0 failures.
