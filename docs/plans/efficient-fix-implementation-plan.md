# Efficient Fix Implementation Plan

**Date:** 2026-07-11
**Goal:** Solve remaining 118 test failures by fixing cascading dependencies first
**Strategy:** Each phase unlocks the next — fix the root causes of cascading failures before addressing surface-level errors

---

## Cascading Dependency Map

```
Phase 1 ─── WebSocketFlowIT missing @AutoConfigureMockMvc (6 tests)
                  │
                  ▼
            Testcontainers container contamination between FlowIT classes
                  │
           ┌──────┴──────────┐
           ▼                  ▼
    DataIntegrityViolation   ClassCastException
    (cleanup FK failures)    (String→User in AdminFlowIT)
           │
           ▼
    74 ApplicationContext cascade failures across 10 FlowIT classes
           │
           ▼
    (Phase 1 done → cascade eliminated)
           │
     ┌─────┴──────────────────────────┐
     ▼                                ▼
Phase 2                        Phase 3
NPEs (21 tests)                Wrong HTTP status (8 tests)
  └ UserMapperTest (16)          └ PageImpl 500 (2)
  └ PostMapperTest (4)            └ Security 401 vs 200 (4)
  └ AuthServiceTest (1)           └ Validation 400 (2)
  └ PostRepositoryTest (1)
                                      │
                                      ▼
                                 Phase 4
                                 Individual issues (5 tests)
                                   └ Mockito stubbing
                                   └ EntityNotFoundException
                                   └ CommentMapper quoting
                                   └ WireMock port
                                   └ UserMapper IllegalArgumentException
```

---

## Phase 1 — Eliminate Cascading Failures

**Target:** Fix the 74 `ApplicationContext failure threshold exceeded` cascade failures by resolving their root causes. When Phase 1 is complete, ALL 74 cascade failures disappear — the other ~44 real failures become independently visible.

### Step 1.1: WebSocketFlowIT — Add @AutoConfigureMockMvc

**Problem:** `WebSocketFlowIT` has `@Autowired MockMvc mockMvc` but `@SpringBootTest(webEnvironment = RANDOM_PORT)` without `@AutoConfigureMockMvc`. Injecting MockMvc fails → context fails to load → all 6 WebSocket tests cascade.

**Rationale for priority:** This is the simplest fix (add one annotation) and immediately eliminates 6 cascade failures. It also prevents WebSocketFlowIT from corrupting the Testcontainers state for subsequent classes.

**Fix:** Add to `src/test/java/dev/thural/quietspace/websocket/WebSocketFlowIT.java`:

```java
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class WebSocketFlowIT { ... }
```

**Effort:** 1 file, 2 lines, 5 minutes

---

### Step 1.2: Fix FlowIT @BeforeEach Cleanup (DataIntegrityViolation)

**Problem:** The `@BeforeEach` method in most FlowITs calls `userRepository.deleteAll()`, which fails with FK constraint violations because related tables (`user_followings`, `user_chat`, `Token`) still reference users. The failed cleanup leaves stale data → subsequent FlowIT classes fail context load → 74 cascade failures.

**Root cause entities:**
```
User ──→ ProfileSettings     (cascade = ALL)      ← delete works
User ──→ Post                (cascade = ALL)      ← delete works
User ──→ Comment             (cascade = ALL)      ← delete works
User ──→ Message             (cascade = ALL)      ← delete works
User ──→ Chat (via user_chat)(cascade = MERGE,PERSIST,REFRESH) ← delete FAILS
User ──→ User.followings     (no cascade)         ← delete FAILS
User ←── Token (via userId)  (no cascade)         ← delete FAILS
```

**Fix:** Replace `userRepository.deleteAll()` with FK-safe ordered deletion. If a FlowIT has access to `EntityManager`, use:

```java
// In @BeforeEach, before userRepository.deleteAll():
tokenRepository.deleteAll();
entityManager.createNativeQuery("DELETE FROM user_followings").executeUpdate();
entityManager.createNativeQuery("DELETE FROM user_chat").executeUpdate();
profileSettingsRepository.deleteAll();
userRepository.deleteAll();
```

Since not all FlowITs inject all repositories, create a shared cleanup utility:

**Option A — Add a `cleanAll()` method to `IntegrationTestHelper`:**
```java
public void cleanAll(EntityManager entityManager) {
    entityManager.createNativeQuery("DELETE FROM token").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM user_followings").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM user_chat").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM user_saved_posts").executeUpdate();
}
```

**Option B — Modify each FlowIT to inject `EntityManager` and clean in `@BeforeEach`:**

```java
@Autowired
private EntityManager entityManager;

@BeforeEach
void setUp() throws Exception {
    entityManager.createNativeQuery("DELETE FROM user_followings").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM user_chat").executeUpdate();
    entityManager.createNativeQuery("DELETE FROM user_saved_posts").executeUpdate();
    userRepository.deleteAll();
    // ... rest of setup
}
```

**Option C — Use `@Sql` annotation at class level:**
```java
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
```

**Effort:** 1 utility method + update 9 FlowITs, 30 minutes

---

### Step 1.3: Fix AdminFlowIT Context Load (ClassCastException)

**Problem:** AdminFlowIT fails context load with `ClassCastException: class java.lang.String cannot be cast to User`. This happens when `UserDetailsService.loadUserByUsername()` returns a `String` username instead of a `User` entity. This is likely caused by the `@MockitoBean` for `UserDetailsService` in AdminControllerTest or similar slice tests contaminating the shared ApplicationContext.

**Actually — this may be a Spring context caching issue.** If AdminControllerTest (a `@WebMvcTest` slice) and AdminFlowIT (a `@SpringBootTest`) share the same ApplicationContext, the mocked `UserDetailsService` from the slice test could persist.

**Fix:** Investigate whether this is caused by context contamination from slice tests. If so, ensure slice tests use separate/unique context configuration from FlowITs.

**Effort:** Investigation, 10–20 minutes

---

### Verification: Phase 1 Complete

After Steps 1.1–1.3:
```
./gradlew clean test
```
Expected: **~44 remaining failures** (74 cascade failures eliminated)

---

## Phase 2 — Fix NullPointerExceptions (21 tests)

**Target:** 21 NPE failures across mapper, service, and repository tests. All caused by null entity fields that Lombok `@Builder` doesn't initialize.

### Step 2.1: User Entity — Add @Builder.Default Initializers

**Problem:** `UserMapper` calls `user.getProfileSettings().getIsPrivateAccount()` but `profileSettings` is null because Lombok `@Builder` doesn't initialize fields unless `@Builder.Default` is specified. Same for `savedPosts` and `followers`/`followings`.

**Fix in `User.java` entity (3 field changes):**

```java
@JsonIgnore
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
@Builder.Default
ProfileSettings profileSettings = new ProfileSettings();

@JsonIgnore
@Builder.Default
private List<Post> savedPosts = new ArrayList<>();

@JsonIgnore
@ManyToMany
@JoinTable(name = "user_followings", ...)
@Builder.Default
private List<User> followings = new ArrayList<>();

@JsonIgnore
@Builder.Default
@ManyToMany(mappedBy = "followings")
private List<User> followers = new ArrayList<>();
```

**Note:** `followings` and `followers` already have `@Builder.Default` (lines 89, 93 in User.java). But `profileSettings` and `savedPosts` don't.

**Tests fixed:** 16 `UserMapperTest` + 1 `PostRepositoryTest` = 17 tests

**Effort:** 1 file, 3 annotations, 10 minutes

---

### Step 2.2: PostMapper — Null Check for Poll

**Problem:** `PostMapper` calls `post.getPoll().getOptions()` without null check. When `Post` is built without a `Poll`, this NPEs.

**Fix in `PostMapper`:**

```java
if (post.getPoll() != null) {
    // existing poll handling code
}
```

**Tests fixed:** 4 `PostMapperTest` tests

**Effort:** 1 file, 2–3 lines, 5 minutes

---

### Step 2.3: AuthServiceTest — Fix Mock Answer

**Problem:** `UserDetailsService.loadUserByUsername()` is mocked but returns `null`.

**Fix in test:**

```java
User mockUser = User.builder()
    .id(UUID.randomUUID())
    .username("testuser")
    .email("test@test.com")
    .password("password")
    .role(Role.USER)
    .build();
when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mockUser);
```

**Tests fixed:** 1 `AuthServiceTest.signout`

**Effort:** 1 file, 5 lines, 5 minutes

---

### Verification: Phase 2 Complete

```
./gradlew clean test
```
Expected: **~23 remaining failures** (21 NPEs fixed)

---

## Phase 3 — Fix Wrong HTTP Status Assertions (8 tests)

### Step 3.1: PageImpl Serialization (2 tests → 500)

**Problem:** `AdminControllerTest.getPagedUsers` and `ReactionControllerTest.getReactionsByContent` expect 200 but get 500. The `@EnableSpringDataWebSupport(VIA_DTO)` added to `QuietspaceApplication` is not picked up by `@WebMvcTest` slice tests.

**Fix:** Add `@EnableSpringDataWebSupport` to each slice test's inner `@TestConfiguration`:

```java
@TestConfiguration
static class TestConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

Change to:

```java
@TestConfiguration
static class TestConfig {
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
static class WebSupportConfig {}
```

Or import the main application class config.

**Tests fixed:** `AdminControllerTest.getPagedUsers`, `ReactionControllerTest.*` (2 tests)

**Effort:** 2 files, 6 lines, 10 minutes

---

### Step 3.2: PostControllerSecurityTest (4 tests → wrong status)

**Problem:** `@WebMvcTest(controllers = PostController.class)` without `@AutoConfigureMockMvc(addFilters = false)` should have active security filters, but requests without tokens return 200/204/404/415 instead of 401.

**Root cause investigation needed:** In Spring Boot 4.x, the `@WebMvcTest` security auto-configuration may behave differently. The `JwtFilter` is loaded (beans are mocked), but the Spring Security filter chain may not be registered.

**Potential fixes:**
1. Ensure `SecurityConfig` or equivalent is imported in the test
2. Check if `@WebMvcTest` auto-configures `SecurityAutoConfiguration` in SB 4.x
3. Use `@Import(SecurityConfig.class)` on the test class
4. If security truly cannot be tested with `@WebMvcTest`, convert to `@SpringBootTest`

**Tests fixed:** 4 `PostControllerSecurityTest` tests

**Effort:** Investigation + fix, 15–30 minutes

---

### Step 3.3: Validation Errors (2 tests → 400)

**Problem:** `slice.UserControllerTest.patchUser` and `unit.UserControllerTest.patchUser` expect 200 but get 400. The `@Valid` annotation on `patchUser()` rejects the request body.

**Fix:** Verify the request body meets all validation constraints (field lengths, patterns, required fields). Adjust the test request or controller validation accordingly.

**Tests fixed:** 2 `UserControllerTest.patchUser` tests

**Effort:** 10 minutes

---

### Verification: Phase 3 Complete

```
./gradlew clean test
```
Expected: **~15 remaining failures** (8 assertion errors fixed)

---

## Phase 4 — Fix Individual Issues (5 tests)

### Step 4.1: Mockito PotentialStubbingProblem
**File:** `NotificationControllerTest.getAllNotifications_shouldReturnPage()`
**Fix:** Change `anyInt()` stub to `any()` or fix invocation to pass integers.

### Step 4.2: EntityNotFoundException → 404
**File:** `PhotoControllerTest.getPhotoByName_whenNotFound_shouldReturn404()`
**Fix:** Add `@ExceptionHandler(EntityNotFoundException.class)` returning 404.

### Step 4.3: CommentMapper String Quoting
**File:** `CommentMapperTest.commentEntityToResponse_shouldHandleDifferentReactionTypes()`
**Fix:** Change assertion from `.isEqualTo("DISLIKE")` to `.isEqualTo(DISLIKE)` (enum comparison).

### Step 4.4: UserMapper IllegalArgumentException
**File:** `UserMapperTest.toSettingsResponse_shouldConvertUserToSettingsResponse()`
**Fix:** This is fixed by Phase 2 Step 2.1 (builder defaults for `profileSettings`). Verify after Phase 2.

### Step 4.5: WireMock Verification
**File:** `EmailServiceIT.sendEmail_shouldDeliverSuccessfully()`
**Fix:** Verify WireMock port matches `spring.mail.port` and ensure stub is registered before service call.

---

## Summary: Execution Order

```
Phase 1 (Eliminate cascading)
├── 1.1 WebSocketFlowIT @AutoConfigureMockMvc          [5 min]
├── 1.2 FlowIT cleanup FK-safe delete order            [30 min]
└── 1.3 AdminFlowIT ClassCastException investigation   [20 min]

Phase 2 (Fix NPEs)
├── 2.1 User entity @Builder.Default annotations       [10 min]
├── 2.2 PostMapper null check for Poll                 [5 min]
└── 2.3 AuthServiceTest mock answer                    [5 min]

Phase 3 (Fix HTTP status)
├── 3.1 PageImpl serialization in @WebMvcTest          [10 min]
├── 3.2 PostControllerSecurityTest security config     [30 min]
└── 3.3 UserControllerTest @Valid validation           [10 min]

Phase 4 (Individual fixes)
├── 4.1 Mockito stubbing mismatch                      [5 min]
├── 4.2 EntityNotFoundException handler                [10 min]
├── 4.3 CommentMapper assertion type                   [2 min]
├── 4.4 UserMapper (fixed by Phase 2)                  [0 min]
└── 4.5 WireMock port verification                     [10 min]

Total estimated effort: ~2.5 hours
```

## Expected Outcome by Phase

| Phase | Tests Fixed | Cumulative Fixed | Remaining |
|-------|------------|-----------------|-----------|
| Start | — | — | 118 |
| Phase 1 | 74 cascade + 6 WebSocket + 12 FlowIT real | 92 | 26 |
| Phase 2 | 21 NPE | 113 | 5 |
| Phase 3 | 8 assertion | 121 | 0* |
| Phase 4 | 5 individual | 126 | 0* |

*Target: 0 remaining failures. If some Phase 3/4 fixes require deeper investigation, 3–5 may persist.

## Files to Modify

| Phase | File | Change |
|-------|------|--------|
| 1.1 | `websocket/WebSocketFlowIT.java` | Add `@AutoConfigureMockMvc` |
| 1.2 | `controller/UserFlowIT.java` | Add FK-safe cleanup in `@BeforeEach` |
| 1.2 | `controller/AuthFlowIT.java` | Add FK-safe cleanup in `@BeforeEach` |
| 1.2 | `controller/PostFlowIT.java` | Add FK-safe cleanup in `@BeforeEach` |
| 1.2 | `utils/IntegrationTestHelper.java` | Add `cleanAll()` method |
| 1.2 | All other FlowITs (6 files) | Add FK-safe cleanup |
| 2.1 | `entity/User.java` | Add `@Builder.Default` to 2 fields |
| 2.2 | `mapper/PostMapper.java` | Add null check for `poll` |
| 2.3 | `AuthServiceTest.java` | Add mock answer |
| 3.1 | `controller/AdminControllerTest.java` | Add `@EnableSpringDataWebSupport` |
| 3.1 | `controller/ReactionControllerTest.java` | Add `@EnableSpringDataWebSupport` |
| 3.2 | `slice/PostControllerSecurityTest.java` | Add security config import |
| 3.3 | `slice/UserControllerTest.java` | Fix request body validation |
| 3.3 | `unit/UserControllerTest.java` | Fix request body validation |
| 4.1 | `NotificationControllerTest.java` | Fix stub/invocation |
| 4.2 | Controller advice or `PhotoController.java` | Add exception handler |
| 4.3 | `CommentMapperTest.java` | Fix assertion |
| 4.5 | `application-testcontainers.yml` or test | Fix WireMock port |

**Total: ~20 files**
