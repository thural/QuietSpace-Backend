# Test Guideline Remediation Plan

Audit date: 2026-07-12  
Baseline: 503 tests passing  
Guidelines: `docs/tests/integration-test-guidelines.md`, `docs/tests/unit-test-guidelines.md`

---

## Critical Context from Audit

### Two separate `AuthControllerTest` files exist
There is a **slice test** at `controller/slice/AuthControllerTest.java` (uses `@WebMvcTest`, needs the `ObjectMapper` `@TestConfiguration` — Tier 2.1) and a **unit test** at `authentication/controller/AuthControllerTest.java` (uses `@ExtendWith(MockitoExtension.class)`, belongs in `controller/unit/` — Tier 3.2). Do not confuse them.

### 500-assertion tests may reveal real controller bugs
Items 0.1 and 0.2 assert `is5xxServerError()` despite stubbing service methods to return valid data (`Page.empty()`). After changing the assertion to `isOk()`, the test may **fail** if the controller endpoint has an actual bug. In that case, fix the **controller**, not the test. The 500 assertion was likely written to match observed broken behavior.

### `@WithUserDetails` in a `MockitoExtension` test is a no-op
Item 0.5: `CommentControllerTest` uses `@ExtendWith(MockitoExtension.class)` + `MockMvcBuilders.standaloneSetup()`. There is no Spring `SecurityContext` in play, so `@WithUserDetails` never populates the user. Remove the annotation and use `SecurityContextHolder.getContext().setAuthentication(...)` in `@BeforeEach` instead, or switch the test to a slice test.

### `WsDiagnosticTest` was created for debugging and never cleaned up
Item 0.6 and 1.2: This file was added as an untracked diagnostic helper and was never intended to be a permanent test. It has zero assertions. Delete it rather than rename it.

### `AuthFlowIT` MockMvc override (1.5)
Lines 73–76 contain:
```java
mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
```
This replaces the `MockMvc` that `@AutoConfigureMockMvc` provides at field-injection time. Remove this `@Autowired WebApplicationContext wac` field and the `@BeforeEach` `configure()` method entirely; the auto-configured `MockMvc` already has security applied when `addFilters` is not set to `false`.

### `WebSocketFlowIT` and `@Transactional` (1.4)
The test currently uses `TransactionTemplate` + `@AfterEach` to work around the lack of `@Transactional`. Adding `@Transactional` to the class allows removing both the `TransactionTemplate` field, the `PlatformTransactionManager` field, and the entire `tearDown()` method — the transaction rolls back automatically after each test.

### `PostControllerSecurityTest` needs specific mock list (1.1)
When converting to `@WebMvcTest`, it must mock `PostService`, `NotificationService`, `TokenRepository`, `JwtService`, and `UserDetailsService`. The first two are the controller's service deps; the last three are `JwtFilter` deps required by Spring Boot 4.x `@WebMvcTest`.

### No `@DirtiesContext` anywhere — good
No test in the project uses `@DirtiesContext`. This should be preserved.

---

## Tier 0 — Wrong Assertions / Broken Test Logic

These are **incorrect tests** that silently pass for the wrong reason. They mask real bugs or assert nothing at all.

| # | File | Issue | Fix |
|---|------|-------|-----|
| 0.1 | `PostControllerTest` (unit) | Lines 109, 124: asserts `is5xxServerError()` but stubs return `Page.empty()` — a valid 200 response | Change to `isOk()` or fix the stub |
| 0.2 | `MessageControllerTest` (unit) | Line 175: same pattern — `getMessagesByChatId` stubs `Page.empty()` but expects 500 | Change to `isOk()` |
| 0.3 | `UserControllerTest` (unit) | Line 119: `patch(path, registerRequest)` — second arg is a URI template variable, not a request body; the `registerRequest` is silently dropped | Remove second positional arg; body is already passed via `.content(userBodyJson)` |
| 0.4 | `CommentControllerTest` (unit) | `deleteComment()` — empty method body | Implement or remove |
| 0.5 | `CommentControllerTest` (unit) | `@WithUserDetails` on `createComment()` — requires Spring Security context but test uses `MockitoExtension` | Remove annotation; use `SecurityContextHolder` mock or `@WithMockUser` with `SpringExtension` |
| 0.6 | `WsDiagnosticTest` | Uses `System.out.println` instead of assertions | Remove file or convert to proper assertions |

**Why first:** Tier 0 tests are actively misleading — they can pass when the code is broken.

---

## Tier 1 — Structural Integration Test Fixes

These fix expensive context boots, missing Testcontainers connections, and improper test isolation.

| # | File | Issue | Fix |
|---|------|-------|-----|
| 1.1 | `PostControllerSecurityTest` | Uses `@SpringBootTest` (boots full app + MySQL) for a simple `401` filter test | Replace with `@WebMvcTest(PostController.class)` + `@MockitoBean` for `TokenRepository`, `JwtService`, `UserDetailsService`, `PostService`, `NotificationService` |
| 1.2 | `WsDiagnosticTest` | Named `*Test.java` with `@SpringBootTest(RANDOM_PORT)` + Testcontainers — won't be picked up by `integrationTest` task | Rename to `WsDiagnosticIT.java` or delete |
| 1.3 | `QuietspaceApplicationIT` | Missing `@Import(TestcontainersConfig.class)` and `@ActiveProfiles("testcontainers")` — runs against H2 instead of MySQL | Add both annotations per guideline template |
| 1.4 | `WebSocketFlowIT` | Missing `@Transactional`; uses manual `TransactionTemplate` for cleanup | Add `@Transactional` to class, simplify `@BeforeEach`/`@AfterEach` |
| 1.5 | `AuthFlowIT` | Lines 73–76: overrides auto-configured `MockMvc` with `MockMvcBuilders.webAppContextSetup(...)`, defeating `@AutoConfigureMockMvc` | Remove manual MockMvc build; use injected `MockMvc` directly |
| 1.6 | `UserServiceIT` | Missing `webEnvironment = RANDOM_PORT`, `@Transactional`, `@MockitoBean` for `PhotoService` | Add all three |
| 1.7 | `EmailServiceIT` | Missing `webEnvironment = RANDOM_PORT`, `@MockitoBean` for `PhotoService` | Add both |
| 1.8 | `AuthFlowIT` | Missing `@MockitoBean` for `PhotoService` | Add mock |

**Why second:** Tier 1 cuts test execution time significantly (removes full-context boot for security tests, ensures proper DB isolation, adds missing external-service mocks).

---

## Tier 2 — Missing Slice Annotations

| # | File | Issue | Fix |
|---|------|-------|-----|
| 2.1 | `AuthControllerTest` (slice) | Missing static inner `@TestConfiguration` with `ObjectMapper` bean | Add per guideline §3.1 |

**Why third:** Low risk, easy fix, but required for compliance.

---

## Tier 3 — Package Organization

| # | File | Issue | Fix |
|---|------|-------|-----|
| 3.1 | `PhotoControllerTest` | Resides in `controller/` instead of `controller/unit/` | Move to `dev.thural.quietspace.controller.unit` |
| 3.2 | `AuthControllerTest` (unit version) | Resides in `authentication.controller` instead of `controller/unit/` | Move to `dev.thural.quietspace.controller.unit` |

**Why fourth:** Ensures proper Gradle task filtering (`controller/unit/` for unit tests, `controller/slice/` for slice tests).

---

## Tier 4 — Naming Conventions (Widespread)

| # | Category | Count | Fix |
|---|----------|-------|-----|
| 4.1 | Methods using `test*` prefix instead of `methodName_givenScenario_shouldExpectedBehavior` | ~26 methods across 6 service test files | Rename each method |
| 4.2 | Missing AAA blank-line separation between Arrange/Act/Assert | ~32 methods across 5 controller-unit files | Add blank lines |
| 4.3 | Empty / comment-only method bodies with no assertions | `ChatMapperTest` (3 methods), `CommentMapperTest` (6 methods), `CommentControllerTest` (1 method) | Implement or remove |

**Affected files:**
- `ChatServiceImplTest` — `testFindChatById`, `testGetChatsByUserId`, `testDeleteChatById`, `testAddMember`, `testRemoveMember`, `testCreateChat`
- `CommentServiceImplTest` — `testGetCommentsByPost`, `testGetCommentsByUser`, `testCreateComment`, `testGetCommentById`, `testUpdateComment`, `testDeleteComment`, `testPatchComment`
- `MessageServiceImplTest` — `testAddMessage`, `testDeleteMessage`, `testGetMessagesByChatId`, `testGetLastMessageByChat`
- `PostServiceImplTest` — `testGetAllPosts`, `testGetPostByUserId`, `testAddPostById`, `testGetVotedOptionLabel`, `testGetPostResponseById`, `testUpdatePost`, `testVotePoll`, `testDeletePost`
- `ReactionServiceImplTest` — `testHandleReactionRemoveLike`, `testHandleReactionAddLike`
- `UserServiceImplTest` — `testGetUserById`, `testGetUserResponseById`, `testListAllTest`, `testGetUsersFromIdList`, `testGetSignedUser`, `testGetSignedUserResponse`, `testDeleteUser`, `testUpdateUser`

**Why fifth:** High volume, low risk. Can be automated with search-and-replace in bulk.

---

## Tier 5 — Anti-Patterns (Minor)

| # | File | Issue | Fix |
|---|------|-------|-----|
| 5.1 | `NotificationMapperTest` | Lines 166–185: `for` loop over `NotificationType.values()` with `reset()` inside | Replace with `@ParameterizedTest` or inline separate test methods |
| 5.2 | `ChatMapperTest` | 3 empty/comment-only methods (lines 248–275) | Remove or implement with assertions |
| 5.3 | `CommentMapperTest` | 6 empty/comment-only methods (lines 276–321) | Remove or implement with assertions |

**Why last:** Low impact, no risk of masking bugs.

---

## Execution Order

```
Tier 0 (wrong assertions)       → ~6 files, fixes incorrect test behavior
Tier 1 (structural IT fixes)    → ~8 files, cuts runtime, fixes isolation
Tier 2 (missing annotations)    →  1 file, low-effort compliance
Tier 3 (package moves)          →  2 files, requires import adjustments
Tier 4 (naming + AAA)           → ~9 files, high-volume mechanical renames
Tier 5 (anti-patterns)          →  3 files, remove dead code
```

Run `./gradlew test` after each tier to verify zero regressions.
