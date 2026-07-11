# Test Failure Remediation Plan

**Date:** 2026-07-11
**Scope:** All ~151 remaining test failures across slice, FlowIT, and unit tests
**References:** `docs/tests/integration-test-guidelines.md`

---

## Overview

Three systemic root causes account for ~90% of failures. Each has a single, repeatable fix.

| # | Root Cause | Tests Affected | Fix |
|---|-----------|----------------|-----|
| A | `@SpringBootTest(webEnvironment = RANDOM_PORT)` without `@AutoConfigureMockMvc` | 9 FlowIT classes (~40 tests) | Add `@AutoConfigureMockMvc` |
| B | `@WebMvcTest` loads `JwtFilter` which depends on beans not mocked | 6 slice test classes (~30 tests) | Add `@MockitoBean` for `TokenRepository`, `JwtService`, `UserDetailsService` |
| C | Mock `UserResponse` / `ChatResponse` built with only `id` field — JSON path assertions fail | 4 test classes (~10 assertions) | Populate all expected fields in `setUp()` |

Remaining ~30 failures are scattered pre-existing issues (NPE in service tests, `PageImpl` serialization warnings in standalone MockMvc tests, etc.) — each requires individual diagnosis.

---

## Tier 1 — Systemic Fixes (highest impact, lowest effort)

### 1.1 Add `@AutoConfigureMockMvc` to FlowIT classes

**Problem:** All 9 `*FlowIT.java` classes use `@Autowired MockMvc mockMvc` with `@SpringBootTest(webEnvironment = RANDOM_PORT)`. In Spring Boot 4.x, this does **not** auto-configure a `MockMvc` bean — `@AutoConfigureMockMvc` is required.

**Fix:** Add the import and annotation to each file:

```java
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class UserFlowIT { ... }
```

**Files to change** (9):

| File | Annotation to add |
|------|-------------------|
| `controller/AdminFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/ChatFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/CommentFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/MessageFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/NotificationFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/PhotoFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/PostFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/ReactionFlowIT.java` | `@AutoConfigureMockMvc` |
| `controller/UserFlowIT.java` | `@AutoConfigureMockMvc` |

**Note:** `AuthFlowIT.java` already works by building MockMvc manually via `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build()`. No change needed. `WebSocketFlowIT.java` also uses `@Autowired MockMvc mockMvc` and will need the annotation if its `setUp()` is called.

**Verification:** After fix, each FlowIT should load its application context (will still fail on Docker connectivity until Tier 3).

### 1.2 Fix `@WebMvcTest` slice tests missing required mock beans

**Problem:** `JwtFilter` is a Spring bean loaded by `@WebMvcTest` because Spring Boot 4.x's `@WebMvcTest` no longer auto-excludes filter beans. The filter requires `TokenRepository`, `UserDetailsService`, and `JwtService` in its constructor. Tests that lack `@MockitoBean` for these fail at context load.

**Fix:** Add `@MockitoBean` for all three `JwtFilter` dependencies to every slice test that doesn't already have them.

**Template addition** for `controller/slice/AdminControllerTest.java`:
```java
@MockitoBean
private TokenRepository tokenRepository;
@MockitoBean
private JwtService jwtService;
@MockitoBean
private UserDetailsService userDetailsService;
```

**Files to change** (6 — classes that currently lack these mocks):

| File | Already has | Missing |
|------|------------|---------|
| `slice/AdminControllerTest.java` | — (only `UserService`) | `TokenRepository`, `JwtService`, `UserDetailsService` |
| `slice/AuthControllerTest.java` | — (none listed) | `TokenRepository`, `JwtService`, `UserDetailsService` |
| `slice/NotificationControllerTest.java` | — | all three |
| `slice/PhotoControllerTest.java` | — | all three |
| `slice/PostControllerSecurityTest.java` | — | all three |
| `slice/ReactionControllerTest.java` | — | all three |

**Already correct:** `ChatControllerTest`, `MessageControllerTest`, `PostControllerTest`, `UserControllerTest`, `CommentControllerTest` already declare these beans.

**Note:** `PostControllerSecurityTest` intentionally tests security and does **not** use `addFilters = false`. Adding `@MockitoBean` for the filter's dependencies is still necessary for the bean to be created.

### 1.3 Fix JSON path assertion failures in Chat mock response data

**Problem:** `setUp()` in `ChatControllerTest` builds `UserResponse.builder().id(...).build()` which only sets `id`. Fields like `username`, `role`, `email` are null and omitted by Jackson serialization. JSON path assertions like `$.members[0].username` fail with `PathNotFoundException`.

**Fix:** Populate all expected fields in the mock `UserResponse` objects in `setUp()`.

**Before (`ChatControllerTest.java`):**
```java
this.userResponse1 = UserResponse.builder()
    .id(user1.getId())
    .build();
```

**After:**
```java
this.userResponse1 = UserResponse.builder()
    .id(user1.getId())
    .username(user1.getUsername())
    .email(user1.getEmail())
    .role(user1.getRole())
    .build();
```

**Repeat for** `userResponse2`, `userResponse3`, and the response in `addMemberWithId` tests.

Also check `userResponse` in `UserControllerTest` and `CommentControllerTest` for similar patterns in their `setUp()` methods.

---

## Tier 2 — Guidelines Document Updates

The existing `docs/tests/integration-test-guidelines.md` has two outdated sections that should be corrected to reflect Spring Boot 4.x behavior:

### 2.1 FlowIT template missing `@AutoConfigureMockMvc`

**Current (line 127–164):** Shows `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `@Autowired MockMvc mockMvc` but no `@AutoConfigureMockMvc`.

**Fix:** Add `@AutoConfigureMockMvc` to the template and note that it is required in Spring Boot 4.x.

### 2.2 Slice test template shows old annotation package

**Current (line 55–73):** Shows `@AutoConfigureMockMvc(addFilters = false)` / `@WebMvcTest(controllers = ...)`. The imports use the *outdated* Spring Boot 3.x package `org.springframework.boot.test.autoconfigure.web.servlet.*`.

**Fix:** Update imports to the Spring Boot 4.x correct package:
```java
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
```

Also add a note that `JwtFilter` dependencies must be mocked in every slice test.

---

## Tier 3 — Docker / Testcontainers Verification

### 3.1 Verify Testcontainers 1.21.4 works

**Status:** Testcontainers `1.20.4 → 1.21.4` upgrade is already applied in `build.gradle.kts`.

**Next step:** After fixing Tier 1 (MockMvc), run one FlowIT to verify MySQL container starts:
```bash
./gradlew integrationTest --tests "*QuietspaceApplicationIT*"
```

### 3.2 Run full FlowIT suite

Once MockMvc and Testcontainers both work:
```bash
./gradlew integrationTest
```

Expected: ~40 `*FlowIT` tests pass (controllers → real MySQL via Docker).

---

## Tier 4 — Remaining Failures (post Tier 1–3 remediation)

After implementing Tiers 1–3, **118 failures remain** (from ~513 total tests). These break down into a few systemic categories + scattered individual issues.

### 4.1 Cascading ApplicationContext Failures (74 tests / 62.7%)

These are **not real failures**. When a FlowIT test class's first test fails to load the Spring context (due to a real error), JUnit reports all subsequent tests in that class as `ApplicationContext failure threshold exceeded`. The true root causes are documented below.

**Affected classes:** `AdminFlowIT`, `AuthFlowIT`, `ChatFlowIT`, `CommentFlowIT`, `MessageFlowIT`, `NotificationFlowIT`, `PhotoFlowIT`, `PostFlowIT`, `ReactionFlowIT`, `UserFlowIT`, `WebSocketFlowIT`

When run in isolation (e.g., `--tests "*UserFlowIT"`), these classes expose their real failures:
- `AuthFlowIT` / `PostFlowIT` / `UserFlowIT`: `DataIntegrityViolationException` — FK constraints prevent `deleteAll()` cleanup in `@BeforeEach`
- `WebSocketFlowIT`: `UnsatisfiedDependencyException` — no MockMvc bean (missing `@AutoConfigureMockMvc`)

**Root cause:** Multiple FlowIT classes share the same Testcontainers MySQL container (via `@TestConfiguration(proxyBeanMethods = false)` + `@ServiceConnection`). When one class fails to clean up, the corrupted schema state causes subsequent classes to fail context loading.

**Fix:** Isolate test container instances per class, or ensure cleanup is robust.

---

### 4.2 NullPointerException (21 tests / 17.8%)

| Test Class | Root Cause | Fix |
|-----------|------------|-----|
| `UserMapperTest` (16 tests) | `User` entity built with only `id` — `profileSettings`, `savedPosts` are null | Add `@Builder.Default` initialization to `User` entity fields or populate in test `setUp()` |
| `PostMapperTest` (4 tests) | `Poll` is null in test `Post` entity | Add `@Builder.Default private Poll poll;` or build explicitly |
| `AuthServiceTest.signout()` | Mocked `UserDetailsService` returns `null` user | Add mock answer that returns a valid `User` |
| `PostRepositoryTest` | `user.getSavedPosts()` returns null | Add `@Builder.Default private List<Post> savedPosts = new ArrayList<>();` on `User` entity |

---

### 4.3 Wrong HTTP Status Assertions (8 tests / 6.8%)

| Test | Actual Status | Root Cause |
|------|-------------|------------|
| `AdminControllerTest.getPagedUsers` | 500 | `PageImpl` serialization — `@EnableSpringDataWebSupport(VIA_DTO)` was added but may not be recognized in `@WebMvcTest` slice |
| `ReactionControllerTest.getReactionsByContent` | 500 | Same as above |
| `slice.NotificationControllerTest.getNotificationsByType` | 400 | Validation error — request parameters may be invalid |
| `PostControllerSecurityTest.*` (4 tests) | 200/404/204/415 | Security filters not active — `@WebMvcTest` in SB 4.x may auto-set `addFilters=false` |
| `UserControllerTest.patchUser()` (2 tests: slice + unit) | 400 | `@Valid` validation failing on request body |
| `UserServiceIT.listUsers` | size 0 | Test data not persisted — `@BeforeEach` cleanup deletes seed data |

---

### 4.4 Missing `@AutoConfigureMockMvc` — WebSocketFlowIT (6 tests)

`@SpringBootTest(webEnvironment = RANDOM_PORT)` requires `@AutoConfigureMockMvc` for `@Autowired MockMvc` to work, same as all other FlowITs.

**Fix:** Add `@AutoConfigureMockMvc` to `WebSocketFlowIT.java`.

---

### 4.5 Individual Issues (5 tests)

| Test | Issue | Fix |
|------|-------|-----|
| `NotificationControllerTest.getAllNotifications` | Mockito `PotentialStubbingProblem` — stubbed with `anyInt()` but invoked with `null` | Fix stub or invocation to match |
| `PhotoControllerTest.getPhotoByName` | `EntityNotFoundException` returns 500 instead of 404 | Add `@ExceptionHandler` in controller or use `ResponseEntity` |
| `CommentMapperTest` | String comparison `"DISLIKE"` (with quotes) vs `DISLIKE` | Fix test assertion — `.isEqualTo("DISLIKE")` vs `.isEqualTo(DISLIKE)` |
| `UserMapperTest.toSettingsResponse` | `IllegalArgumentException` | Check for null collection in mapper |
| `EmailServiceIT` | WireMock received no POST request | Check WireMock port config and stubbing order |

---

## Updated Effort Estimate

| Tier | Changes | Files | Est. effort |
|------|---------|-------|-------------|
| 1.1 Add `@AutoConfigureMockMvc` | 9 annotations + imports | 9 | ✅ Done |
| 1.2 Add missing `@MockitoBean` | 18 annotations (3 × 6 classes) + imports | 6 | ✅ Done |
| 1.3 Fix mock response data | Populate builder fields | 2–4 | ✅ Done |
| 2 Update guidelines | Edit sections 3.1, 4 | 1 | ✅ Done |
| 3 Fix Jackson constructors + JWT key | 4 files | 4 | ✅ Done |
| 4.1 Fix cascading ApplicationContext failures | Isolate test containers | 1–2 | 30 min |
| 4.2 Fix NPEs in mappers/repos | Add builder defaults | 2–3 | 20 min |
| 4.3 Fix wrong HTTP status assertions | Various | 4–5 | 30 min |
| 4.4 Add `@AutoConfigureMockMvc` to WebSocketFlowIT | 1 file | 1 | 5 min |
| 4.5 Fix individual issues | Various | 5 | 30 min |

**Remaining effort:** ~2 hours

---

## Recommended Order for Remaining Fixes

```
4.4 (WebSocketFlowIT MockMvc) ──→ 4.2 (NPEs in entity builder defaults)
        │                               │
        ▼                               ▼
4.1 (Testcontainers isolation)     4.3 (HTTP status assertions)
        │
        ▼
4.5 (individual scattered fixes)
```
