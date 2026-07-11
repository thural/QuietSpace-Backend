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

## Tier 4 — Scattered Pre-Existing Failures (lower priority)

These are the remaining ~30 failures not covered by Tiers 1–3:

| Issue | Affected Tests | Likely Fix |
|-------|---------------|------------|
| `PageImpl` serialization warning → 500 | `AdminControllerTest.getPagedUsers()`, `ReactionControllerTest.getReactionsByContent()` | Add `@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)` to `@TestConfiguration` or application config |
| `@ExtendWith(MockitoExtension.class)` alongside `@WebMvcTest` | `MessageControllerTest`, `PostControllerTest` (in `slice/`) | Remove `@ExtendWith(MockitoExtension.class)` — `@MockitoBean` is sufficient; inline `@Captor` fields |
| Duplicate test classes (`controller/unit/` vs `controller/slice/`) | 10 pair of test classes | Evaluate which ones to keep; delete duplicates |
| NPE in mapper tests | `mapper/EntityMapperTest` etc. | Individual diagnosis |
| NPE in repository tests | `repository/PostRepositoryTest` etc. | Individual diagnosis |
| `UserControllerTest.patchUser()` assertion | Line 159 | Already partially fixed (inline captor + `@Valid` + fix `patch(path, body)` → `patch(path)`). Verify. |

---

## Effort Estimate

| Tier | Changes | Files | Est. effort |
|------|---------|-------|-------------|
| 1.1 Add `@AutoConfigureMockMvc` | 9 annotations + imports | 9 | 15 min |
| 1.2 Add missing `@MockitoBean` | 18 annotations (3 × 6 classes) + imports | 6 | 20 min |
| 1.3 Fix mock response data | Populate builder fields | 2–4 | 10 min |
| 2 Update guidelines | Edit sections 3.1, 4 | 1 | 10 min |
| 3 Verify Docker + run FlowIT | Test run | — | 5 min |
| 4 Scattered fixes | Individual diagnosis | ~15 | 1–2 hrs |

**Total:** ~3 hours

---

## Recommended Order

```
Tier 1.1 (MockMvc) ──→ Tier 1.2 (mock beans) ──→ Tier 1.3 (JSON paths)
        │                       │
        ▼                       ▼
Tier 3 (Docker verify)    Tier 2 (update guidelines)
        │
        ▼
Tier 4 (scattered fixes)
```
