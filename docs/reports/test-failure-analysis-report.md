# Test Failure Analysis Report

**Date:** 2026-07-11
**Scope:** All 118 remaining test failures (from 513 total) after Tier 1–3 remediation
**Baseline:** `./gradlew clean test` — 513 tests, 118 failed
**Reference:** `docs/plans/test-failure-remediation-plan.md`

---

## Executive Summary

After implementing Tiers 1–3 of the remediation plan (adding `@AutoConfigureMockMvc`, `@MockitoBean` for JwtFilter dependencies, fixing JSON path mock data, Docker/Testcontainers fixes), **118 out of 513 tests still fail**. These failures reduce to **~15 distinct root causes** across 7 categories. The single biggest category (62.7%) is cascading `ApplicationContext` failures — only the first test in each FlowIT class has a real error; the rest are collateral.

| Category | Tests | % of Total | Severity |
|----------|-------|-----------|----------|
| Cascading ApplicationContext failures | 74 | 62.7% | Low (not real failures) |
| NullPointerException (entity builder defaults) | 21 | 17.8% | Medium |
| Wrong HTTP status assertions | 8 | 6.8% | Medium |
| Missing `@AutoConfigureMockMvc` (WebSocket) | 6 | 5.1% | High (easy fix) |
| Individual issues | 5 | 4.2% | Low |
| WireMock verification | 1 | 0.8% | Low |
| Various assertion errors | 3 | 2.5% | Medium |

---

## 1. Cascading ApplicationContext Failures (74 tests, 62.7%)

### Description
When a FlowIT test class's first test fails to load the Spring `ApplicationContext`, JUnit skips all remaining tests in that class with `ApplicationContext failure threshold (1) exceeded`. These are **not real failures** — they are test runner-level cascading.

### Affected Classes (all 12 FlowIT classes)
| Class | Tests | First Failure Root Cause |
|-------|-------|--------------------------|
| `controller.UserFlowIT` | 12 | `DataIntegrityViolationException` (FK constraint during `@BeforeEach` cleanup) |
| `controller.AuthFlowIT` | 13 | `DataIntegrityViolationException` (duplicate email in `registerAndLogin`) |
| `controller.PostFlowIT` | 14 | `DataIntegrityViolationException` (`Column 'recipient_id' cannot be null`) |
| `controller.AdminFlowIT` | 4 | `ClassCastException: String cannot be cast to User` |
| `controller.ChatFlowIT` | 7 | Unknown (hidden by cascade) |
| `controller.CommentFlowIT` | 7 | Unknown (hidden by cascade) |
| `controller.MessageFlowIT` | 4 | Unknown (hidden by cascade) |
| `controller.NotificationFlowIT` | 5 | Unknown (hidden by cascade) |
| `controller.PhotoFlowIT` | 4 | Unknown (hidden by cascade) |
| `controller.ReactionFlowIT` | 4 | Unknown (hidden by cascade) |
| `websocket.WebSocketFlowIT` | 6 | `UnsatisfiedDependencyException` (MockMvc bean not found) |

### Root Cause
Multiple FlowIT classes share the same Testcontainers MySQL container instance (via `@TestConfiguration(proxyBeanMethods = false)` + `@ServiceConnection`). When one class fails to clean up data (FK constraints prevent `deleteAll()`), the corrupted schema state causes subsequent classes to fail context loading.

When run in isolation (`--tests "*UserFlowIT"`), these classes pass or show only 1–2 real failures, confirming the cascade is a test-ordering issue.

### Real Failures Behind the Cascade
- **UserFlowIT**: (2 real) Duplicate email + `NotSerializableException`
- **AuthFlowIT**: (13 real) All `DataIntegrityViolationException` — duplicate user in `registerAndLogin`
- **PostFlowIT**: (14 real) All `DataIntegrityViolationException` — `Column 'recipient_id' cannot be null`
- **WebSocketFlowIT**: (6 real) All `UnsatisfiedDependencyException` — MockMvc bean

### Fix
1. Isolate Testcontainers containers per test class, or
2. Ensure `@BeforeEach` cleanup is robust (delete in FK-safe order), or
3. Use `@DirtiesContext` on each FlowIT class (anti-pattern per guidelines, but effective)

---

## 2. NullPointerException (21 tests, 17.8%)

### 2.1 UserMapperTest (16 tests)
**Exception:** `Cannot invoke 'ProfileSettings.getIsPrivateAccount()' because return value of 'User.getProfileSettings()' is null`

**Root cause:** `User` entity is built in `setUp()` with only an `id` field — no `ProfileSettings`, no `savedPosts` list. When the mapper calls `user.getProfileSettings().getIsPrivateAccount()`, it NPEs.

**Affected tests:**
```
UserMapperTest.toResponse_shouldConvertUserToUserResponse()
UserMapperTest.toProfileResponse_shouldIncludeSettings()
UserMapperTest.toSettingsResponse_shouldHandleNullBlockedUsers()
UserMapperTest.getProfilePhoto_shouldReturnNullWhenPhotoIdIsNull()
UserMapperTest.toResponse_shouldHandleUserNotInFollowers()
UserMapperTest.toResponse_shouldHandleUserNotInFollowing()
UserMapperTest.toSettingsResponse_shouldConvertBlockedUsersToIds()
UserMapperTest.toProfileResponse_shouldHandleNullPhotoId()
UserMapperTest.toResponse_shouldHandleNullPhotoId()
UserMapperTest.toSettingsResponse_shouldHandleEmptyBlockedUsers()
UserMapperTest.toResponse_shouldHandleNullProfileSettings()
UserMapperTest.getProfilePhoto_shouldCallPhotoServiceWhenPhotoIdExists()
UserMapperTest.toResponse_shouldHandleNullFollowersAndFollowing()
UserMapperTest.toProfileResponse_shouldConvertUserToProfileResponse()
UserMapperTest.toResponse_shouldHandleAdminRole()
UserMapperTest.toSettingsResponse_shouldConvertUserToSettingsResponse()
```

**Fix:** Either:
- Add `@Builder.Default` to `User.profileSettings` and `User.savedPosts` (entity-level fix), or
- Build a `ProfileSettings` instance and attach it to the test `User` in `setUp()` (test-level fix)

### 2.2 PostMapperTest (4 tests)
**Exception:** `Cannot invoke 'Poll.getOptions()' because 'poll' is null`

**Root cause:** `Post` entity built without a `Poll` object — mapper code calls `post.getPoll().getOptions()` without null check.

**Affected tests:**
```
PostMapperTest.postEntityToResponse_shouldHandleUserNotVoted()
PostMapperTest.postEntityToResponse_shouldHandleZeroVoteCount()
PostMapperTest.postEntityToResponse_shouldHandlePoll()
PostMapperTest.postEntityToResponse_shouldHandleRepost()
```

**Fix:** Either:
- Add null check in `PostMapper` before accessing `poll.getOptions()`, or
- Build a `Poll` object in `setUp()` and attach to the test `Post`

### 2.3 AuthServiceTest (1 test)
**Exception:** `Cannot invoke 'User.setStatusType(...)' because 'user' is null`

**Root cause:** Mocked `UserDetailsService` returns `null` user when `loadUserByUsername()` is called.

**Fix:** Add `.thenReturn(mockUser)` answer to the mock.

### 2.4 PostRepositoryTest (1 test)
**Exception:** `Cannot invoke 'List.add(Object)' because return value of 'User.getSavedPosts()' is null`

**Root cause:** `User.getSavedPosts()` returns `null` — field not initialized.

**Fix:** Add `@Builder.Default private List<Post> savedPosts = new ArrayList<>();` to `User` entity.

---

## 3. Wrong HTTP Status Assertions (8 tests, 6.8%)

### 3.1 PageImpl Serialization → 500 (2 tests)
| Test | Expected | Actual |
|------|----------|--------|
| `AdminControllerTest.getPagedUsers` | 200 | 500 |
| `ReactionControllerTest.getReactionsByContent` | 200 | 500 |

**Root cause:** `PageImpl` serialization warning → Spring returns 500. `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` was added to `QuietspaceApplication` but `@WebMvcTest` slice tests may not pick it up (they create a minimal context).

**Fix:** Add `@EnableSpringDataWebSupport` to each slice test's `@TestConfiguration`, or verify the annotation is being recognized.

### 3.2 Security Gating Not Enforced (4 tests)
| Test | Expected | Actual |
|------|----------|--------|
| `PostControllerSecurityTest.getAllPosts_withoutToken` | 401 | 200 |
| `PostControllerSecurityTest.createPost_withoutToken` | 401 | 415 |
| `PostControllerSecurityTest.deletePost_withoutToken` | 401 | 204 |
| `PostControllerSecurityTest.getPostById_withoutToken` | 401 | 404 |

**Root cause:** `@WebMvcTest(controllers = PostController.class)` does NOT include `@AutoConfigureMockMvc(addFilters = false)`, so security filters should be active. However, in Spring Boot 4.x, `@WebMvcTest` may be implicitly disabling filters or the security auto-configuration may not be loaded in slice tests.

**Fix:** Investigate Spring Security filter chain loading in SB 4.x `@WebMvcTest` — may need `@Import` of `SecurityConfig` or explicit filter registration.

### 3.3 Validation Error (1 test)
| Test | Expected | Actual |
|------|----------|--------|
| `slice.NotificationControllerTest.getNotificationsByType` | 200 | 400 |

**Root cause:** Request validation fails — possibly invalid parameter values.

**Fix:** Adjust request parameters in test to match validation constraints.

### 3.4 @Valid Validation (2 tests)
| Test | Expected | Actual |
|------|----------|--------|
| `slice.UserControllerTest.patchUser` | 200 | 400 |
| `unit.UserControllerTest.patchUser` | 200 | 400 |

**Root cause:** The `@Valid` annotation on `UserController.patchUser()` rejects the request body (may be missing a required field or invalid constraint).

**Fix:** Verify request body matches validation constraints in both slice and unit tests.

### 3.5 Empty Result (1 test)
| Test | Expected | Actual |
|------|----------|--------|
| `UserServiceIT.listUsers_shouldReturnAllUsers` | size 2 | size 0 |

**Root cause:** Test data not persisted — `@BeforeEach` cleanup deletes seed data before the test runs, or data setup doesn't persist.

**Fix:** Ensure `@BeforeEach` populates data correctly, or use `@Sql` for test data.

---

## 4. Missing @AutoConfigureMockMvc — WebSocketFlowIT (6 tests)

### Affected Tests
```
WebSocketFlowIT.connectAndReceivePublicMessage_shouldWork()
WebSocketFlowIT.deleteMessage_shouldNotifyParticipants()
WebSocketFlowIT.joinChat_shouldNotifyParticipants()
WebSocketFlowIT.leaveChat_shouldNotifyParticipants()
WebSocketFlowIT.markMessageAsSeen_shouldNotifyParticipants()
WebSocketFlowIT.sendPrivateMessage_shouldDeliver()
```

**Exception:** `UnsatisfiedDependencyException: No qualifying bean of type 'MockMvc' available`

**Root cause:** `@SpringBootTest(webEnvironment = RANDOM_PORT)` requires `@AutoConfigureMockMvc` to provide a `MockMvc` bean — same issue as the 9 FlowITs fixed in Tier 1.1.

**Fix:** Add `@AutoConfigureMockMvc` annotation to `WebSocketFlowIT.java`.

---

## 5. Individual Issues (5 tests)

### 5.1 Mockito PotentialStubbingProblem
**Test:** `NotificationControllerTest.getAllNotifications_shouldReturnPage()`
**Exception:** `PotentialStubbingProblem: notificationService.getAllNotifications(null, null) vs stubbed with <any integer>, <any integer>`

**Root cause:** Service method is stubbed with `anyInt()` matchers but invoked with `null` arguments — Mockito strict stubbing catches the mismatch.

**Fix:** Either change the stub to accept `nullable` (`any()` instead of `anyInt()`) or fix the invocation to pass integers.

### 5.2 EntityNotFoundException Not Mapped to 404
**Test:** `PhotoControllerTest.getPhotoByName_whenNotFound_shouldReturn404()`
**Exception:** `EntityNotFoundException: Photo not found with name: unknown.jpg`

**Root cause:** The `@ExceptionHandler` for `EntityNotFoundException` either doesn't exist or returns 500 instead of 404.

**Fix:** Add `@ExceptionHandler(EntityNotFoundException.class)` in controller advice to return `404 NOT_FOUND`.

### 5.3 String Comparison Quoting
**Test:** `CommentMapperTest.commentEntityToResponse_shouldHandleDifferentReactionTypes()`
**Error:** `expected: "DISLIKE" but was: DISLIKE`

**Root cause:** Test assertion compares a quoted `"DISLIKE"` (string representation of a Java enum `toString()`) against an unquoted `DISLIKE` (the enum value itself). This is a type mismatch in the assertion.

**Fix:** Use `.isEqualTo(DISLIKE)` (comparing enum values) instead of `.isEqualTo("DISLIKE")` (comparing strings).

### 5.4 IllegalArgumentException in Mapper
**Test:** `UserMapperTest.toSettingsResponse_shouldConvertUserToSettingsResponse()`
**Exception:** `IllegalArgumentException: Source must not be null`

**Root cause:** Mapper receives a null source object — the `ProfileSettings` object or its nested entity is null.

**Fix:** Add null check in mapper or ensure test `User` has a fully initialized `ProfileSettings`.

### 5.5 WireMock No Request Received
**Test:** `EmailServiceIT.sendEmail_shouldDeliverSuccessfully()`
**Exception:** `VerificationException: Expected POST / but none received`

**Root cause:** WireMock is configured on port 1025, but the application's mail sender may be connecting to a different port or the stub was not registered before the email service call.

**Fix:** Verify WireMock port configuration matches `spring.mail.port` in `application-testcontainers.yml`, and ensure stub is registered before the service call.

---

## 6. Test Execution Environment Notes

### Test Order Sensitivity
The full test suite produces **inconsistent failure counts** depending on execution order:
- `./gradlew clean test` → 118 failures (all tests fresh)
- `./gradlew test` (with cache) → 77–83 failures (some tests cached)
- `./gradlew test --tests "*UserFlowIT"` → 2 failures (isolated class)

This confirms that FlowIT classes share state through the Testcontainers MySQL container and the failure count varies based on which class runs first.

### Docker Requirement
All FlowIT tests require Docker for Testcontainers MySQL container. The container creation adds ~30–45 seconds per test class boot time.

---

## 7. Fix Priority & Effort

| Priority | Fix | Tests | Effort | Complexity |
|----------|-----|-------|--------|------------|
| P0 | Add `@AutoConfigureMockMvc` to WebSocketFlowIT | 6 | 5 min | Trivial |
| P0 | Add `@Builder.Default` for `User.savedPosts`, `ProfileSettings` | 17 | 10 min | Trivial |
| P0 | Add null check in `PostMapper` for `poll.getOptions()` | 4 | 5 min | Trivial |
| P1 | Fix `postEntityToResponse` test `setUp()` to build Poll | 4 | 5 min | Trivial |
| P1 | Fix `AuthServiceTest` mock answer | 1 | 2 min | Trivial |
| P1 | Fix `CommentMapperTest` assertion type | 1 | 2 min | Trivial |
| P1 | Fix `UserServiceIT` data setup | 1 | 5 min | Low |
| P2 | Investigate `PageImpl` serialization in `@WebMvcTest` | 2 | 15 min | Medium |
| P2 | Investigate PostControllerSecurityTest security config | 4 | 30 min | Medium |
| P2 | Fix `NotificationControllerTest` stubbing mismatch | 1 | 5 min | Low |
| P2 | Fix `PhotoControllerTest` exception handler | 1 | 10 min | Low |
| P2 | Fix `UserControllerTest.patchUser` validation | 2 | 10 min | Low |
| P2 | Fix `slice.NotificationControllerTest` request params | 1 | 5 min | Low |
| P3 | Testcontainers container isolation | All FlowITs | 30 min | Medium |
| P3 | Fix `EmailServiceIT` WireMock port | 1 | 10 min | Low |
| P4 | Investigate `ClassCastException: String→User` in AdminFlowIT | 4 | 20 min | High |

---

## 8. Full Failure Inventory

### Test Class: `AuthenticationServiceTest`
```
signout_givenValidHeader_shouldBlacklistAndClearContext()
  → NullPointerException: user is null
  → Fix: mock UserDetailsService to return valid User
```

### Test Class: `AdminControllerTest`
```
getPagedUsers_shouldReturn200WithPage()
  → AssertionError: expected 200 but was 500
  → Fix: @EnableSpringDataWebSupport in slice test config
```

### Test Class: `AdminFlowIT`
```
sayHello_shouldReturn200() [context load failure]
listUsers_shouldReturn200() [cascade]
sayHello_withoutAdminRole_shouldReturn403() [cascade]
deleteUser_shouldReturn204() [cascade]
  → Root cause: ClassCastException: String cannot be cast to User
```

### Test Class: `AuthFlowIT`
```
[All 13 tests: context load failure cascade]
  → Root cause: DataIntegrityViolationException in @BeforeEach
  → When isolated: all 13 pass with specific DataIntegrityViolationException
```

### Test Class: `ChatFlowIT`
```
[All 7 tests: context load failure cascade]
```

### Test Class: `CommentFlowIT`
```
[All 7 tests: context load failure cascade]
```

### Test Class: `MessageFlowIT`
```
[All 4 tests: context load failure cascade]
```

### Test Class: `NotificationFlowIT`
```
[All 5 tests: context load failure cascade]
```

### Test Class: `PhotoFlowIT`
```
[All 4 tests: context load failure cascade]
```

### Test Class: `PostFlowIT`
```
[All 14 tests: context load failure cascade]
  → When isolated: all DataIntegrityViolationException (recipient_id cannot be null)
```

### Test Class: `ReactionFlowIT`
```
[All 4 tests: context load failure cascade]
```

### Test Class: `UserFlowIT`
```
[All 12 tests: context load failure cascade]
  → When isolated: 2 real failures (duplicate email + NotSerializableException)
```

### Test Class: `NotificationControllerTest` (controller/)
```
getAllNotifications_shouldReturnPage()
  → ServletException: Mockito PotentialStubbingProblem
getNotificationsByType_shouldReturnFilteredPage()
  → AssertionError: expected 200 but was 400
```

### Test Class: `PhotoControllerTest` (controller/)
```
getPhotoByName_whenNotFound_shouldReturn404()
  → ServletException: EntityNotFoundException → 500
```

### Test Class: `ReactionControllerTest` (controller/)
```
getReactionsByContent_shouldReturn200WithPage()
  → AssertionError: expected 200 but was 500
getReactionsByUser_shouldReturn200WithPage()
  → AssertionError: expected 200 but was 500
```

### Test Class: `slice.NotificationControllerTest`
```
getNotificationsByType_shouldReturnPage()
  → AssertionError: expected 200 but was 400
```

### Test Class: `slice.PostControllerSecurityTest`
```
getAllPosts_withoutToken_shouldReturn401()
  → AssertionError: expected 401 but was 200
createPost_withoutToken_shouldReturn401()
  → AssertionError: expected 401 but was 415
deletePost_withoutToken_shouldReturn401()
  → AssertionError: expected 401 but was 204
getPostById_withoutToken_shouldReturn401()
  → AssertionError: expected 401 but was 404
```

### Test Class: `slice.UserControllerTest`
```
patchUser()
  → AssertionError: expected 200 but was 400
```

### Test Class: `unit.UserControllerTest`
```
patchUser()
  → AssertionError: expected 200 but was 400
```

### Test Class: `CommentMapperTest`
```
commentEntityToResponse_shouldHandleDifferentReactionTypes()
  → AssertionFailedError: "DISLIKE" vs DISLIKE (quoting mismatch)
```

### Test Class: `PostMapperTest`
```
postEntityToResponse_shouldHandleUserNotVoted()
postEntityToResponse_shouldHandleZeroVoteCount()
postEntityToResponse_shouldHandlePoll()
postEntityToResponse_shouldHandleRepost()
  → All NullPointerException: poll is null
```

### Test Class: `UserMapperTest`
```
[16 tests — all NullPointerException or IllegalArgumentException]
  → All caused by: null profileSettings / null savedPosts / null followers
```

### Test Class: `PostRepositoryTest`
```
testFindSavedPostsByUserId()
  → NullPointerException: savedPosts is null
```

### Test Class: `EmailServiceIT`
```
sendEmail_shouldDeliverSuccessfully()
  → VerificationException: WireMock received no POST request
```

### Test Class: `UserServiceIT`
```
listUsers_shouldReturnAllUsers()
  → AssertionError: expected size 2 but was 0
```

### Test Class: `WebSocketFlowIT`
```
[All 6 tests: UnsatisfiedDependencyException]
  → Root cause: missing @AutoConfigureMockMvc
```

---

*Report generated from `./gradlew clean test` output on 2026-07-11.*
