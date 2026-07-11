# Integration Test Coverage Assessment — QuietSpace-Backend

> **Date:** 2026-07-11  
> **Scope:** Repository slice (@DataJpaTest), Controller slice (@WebMvcTest), Full integration (@SpringBootTest)  
> **Build Tool:** Gradle (Kotlin DSL)  
> **Test Stack:** JUnit 5, Mockito, Spring Boot Test, H2 (MySQL compatibility mode)

---

## 1. Testing Pyramid — Current Coverage (%)

```
         ╱╲
        ╱ E2E ╲           0% coverage — 0 tests
       ╱────────╲
      ╱  Full    ╲         10% coverage — 1 test (UserControllerIT) + 1 context-load check
     ╱  Stack    ╲
    ╱──────────────╲
   ╱  Controller  ╲        50% coverage — 5 of 10 controllers have @WebMvcTest slices
  ╱   (Slice)     ╲
 ╱────────────────────╲
╱  Repository Layer   ╲     78% coverage — 7 of 9 repositories have @DataJpaTest files
╱──────────────────────╲
╱   Unit Tests (base)  ╲    100% — 34 pure Mockito files covering all services, mappers, utils, security
╱────────────────────────╲
```

---

## 2. Repository Layer — @DataJpaTest (Slice)

### Coverage by Repository

| Repository | Custom Query Methods | @DataJpaTest Present | Methods Tested | Method Coverage |
|---|---|---|---|---|
| `UserRepository` | 6 | ✅ | 5 of 6 | **83%** |
| `TokenRepository` | 5 | ✅ | 5 of 5 | **100%** |
| `ReactionRepository` | 9 | ✅ | 9 of 9 | **100%** |
| `PostRepository` | 5 | ✅ | 2 of 5 | **40%** |
| `MessageRepository` | 3 | ✅ | 2 of 3 | **67%** |
| `CommentRepository` | 6 | ✅ | 5 of 6 | **83%** |
| `ChatRepository` | 2 | ✅ | 2 of 2 | **100%** |
| `NotificationRepository` | 4 | ❌ | 0 of 4 | **0%** |
| `PhotoRepository` | 3 | ❌ | 0 of 3 | **0%** |

**Overall Repository Custom Method Coverage:** 30 of 43 methods tested = **70%**

### Gaps

1. **Missing repositories (0% coverage)** — `NotificationRepository` (4 custom methods) and `PhotoRepository` (3 custom methods) have zero @DataJpaTest coverage. These are straightforward Spring Data derived queries that would benefit from simple smoke tests.

2. **Partial coverage** — `PostRepository` covers only 2 of 5 methods (40%). Missing: `findSavedPostsByUserId`, `findByCommentsUserId`, `deleteByRepostId`. `MessageRepository` misses `findByMessageIdAndChatId`.

3. **Pagination not verified** — Several tests pass `null` as the `Pageable` argument (e.g., `findAllByQuery("sample", null)`), so SQL generation with actual pagination is never verified.

### Pct. Caveat — Repository Layer Integrity

The 70% method-coverage number is misleadingly high because:
- All tests run against **H2** (MySQL compatibility mode), not a real MySQL instance. H2 does not fully support MySQL-specific JSON functions, window functions, or constraint behaviours. This is a **0% production-fidelity gap**.
- `@DataJpaTest` auto-rolls back transactions, so `@AfterEach` cleanup methods are redundant but harmless.

---

## 3. Controller Layer — @WebMvcTest (Slice)

### Coverage by Controller

| Controller | @WebMvcTest Slice | Pure Mockito Unit | Slice Coverage |
|---|---|---|---|
| `PostController` | ✅ | Also has unit test | **100%** |
| `MessageController` | ✅ | Also has unit test | **100%** |
| `UserController` | ✅ | Also has unit test | **100%** |
| `CommentController` | ✅ | Also has unit test | **100%** |
| `ChatController` | ✅ | Also has unit test | **100%** |
| `AuthController` | ❌ | ✅ Only | **0%** |
| `NotificationController` | ❌ | ✅ Only | **0%** |
| `PhotoController` | ❌ | ✅ Only | **0%** |
| `AdminController` | ❌ | ✅ Only | **0%** |
| `ReactionController` | ❌ | ✅ Only | **0%** |

**Overall @WebMvcTest Slice Coverage:** 5 of 10 controllers = **50%**

### Gaps

1. **5 controllers have no @WebMvcTest slice (50% missing)** — `AuthController`, `NotificationController`, `PhotoController`, `AdminController`, `ReactionController` only have pure Mockito unit tests using `MockMvcBuilders.standaloneSetup()`. This means:
   - ⚠️ Spring Security filter chain is not tested for these endpoints
   - ⚠️ Spring MVC exception handlers, converters, and validation are not exercised
   - ⚠️ Jackson serialization/deserialization of request/response bodies is not verified

2. **Security filters disabled on all existing slices (100% security gap)** — All 5 existing @WebMvcTest slices use `@AutoConfigureMockMvc(addFilters = false)`. This means **zero** controller slice tests verify that `@PreAuthorize`, role checks, or JWT validation actually reject unauthorized requests.

### Pct. Caveat — Controller Slice Integrity

- The **50% slice-coverage figure** does not account for the `addFilters = false` anti-pattern. If security verification is considered essential, the effective security-coverage is **0%**.
- 5 controllers tested via `standaloneSetup` MockMvc give **partial coverage** (~30% of what a full @WebMvcTest provides) because they skip filters, converters, exception handlers, and Jackson configuration.

---

## 4. Full Stack Integration — @SpringBootTest

### Coverage by Domain

| Domain | @SpringBootTest Present | Coverage |
|---|---|---|
| User CRUD | ✅ `UserControllerIT` | **100%** (within this domain) |
| Application context boot | ✅ `QuietspaceApplicationTests` | N/A (sanity check) |
| Auth (register → activate → login → JWT → refresh → signout) | ❌ | **0%** |
| Posts | ❌ | **0%** |
| Comments | ❌ | **0%** |
| Messages | ❌ | **0%** |
| Chats | ❌ | **0%** |
| Notifications | ❌ | **0%** |
| Photos | ❌ | **0%** |
| Reactions | ❌ | **0%** |
| WebSocket/STOMP | ❌ | **0%** |
| Admin | ❌ | **0%** |

**Overall Full-Stack Integration Coverage:** 1 domain covered out of 10 = **10%**

### Pct. Caveat — Full Stack Integrity

- The **10% figure** assumes the single `UserControllerIT` is comprehensive within its domain. In reality, it tests only happy-path CRUD (create, read, update, delete, list, search). Error scenarios (invalid input, non-existent IDs, constraint violations, auth failures) are **0% tested** even for the User domain.
- All @SpringBootTest tests use H2, not MySQL. **Production-fidelity is 0%** for database interaction.
- No WireMock is configured, so any external HTTP calls (email sending, third-party APIs) are **0% integration-tested**.
- WebSocket/STOMP endpoints have **0% integration coverage** despite being present in 3 controllers.

---

## 5. Service Layer Integration — Missing (0%)

There are **zero** integration tests that wire a service implementation with its real repository. All service tests use pure Mockito (`@ExtendWith(MockitoExtension.class)`). This means:

- `@Transactional` rollback and propagation behaviour: **0% tested**
- JPA flush, cascade persist/merge, and lazy-loading interaction: **0% tested**
- Hibernate exception translation to business exceptions: **0% tested**
- Multi-entity transactional operations (e.g., creating a post with photos and notifications): **0% tested**

---

## 6. Build Configuration — Gaps

| Aspect | Status | Caveat |
|---|---|---|
| Separate `integrationTest` Gradle task | ❌ **Not configured** | All tests run under a single `test` task. Developers cannot run fast unit tests independently. |
| Testcontainers | ❌ **Not configured** | H2 in MySQL mode is used everywhere, giving **0% production-DB fidelity**. |
| WireMock | ❌ **Not configured** | Any external HTTP dependencies have **0% integration coverage**. |
| @DirtiesContext usage | ✅ None found | Correct — no context caching killed. |
| @MockBean / @MockitoBean | Used in slice tests | Correct for slice tests, but never in @SpringBootTest. |

---

## 7. Anti-Pattern Checklist

| Anti-Pattern | Status | Impact |
|---|---|---|
| @DirtiesContext overuse | ✅ None found | — |
| Mocking repositories in integration tests | ✅ None found | — |
| Live staging service dependencies | ✅ None found | — |
| Data contamination between tests | ⚠️ **At risk** | @DataJpaTest auto-rolls back, but @SpringBootTest (UserControllerIT) does not; manual `@AfterEach` deletes may not fully reset state |
| H2 instead of Testcontainers | ❌ **Present in all tests** | High — MySQL-specific bugs invisible |
| Security bypassed (`addFilters = false`) | ❌ **Present in all 5 @WebMvcTest slices** | High — 0% security verification |
| No separate integration task | ❌ **Present** | Medium — developer velocity impacted |
| @AutoConfigureTestDatabase(replace = NONE) misleading | ❌ **Present in all 7 @DataJpaTest files** | Low — redundant but confusing |
| Duplicate unit + slice tests | ⚠️ **5 controllers have both** | Low — marginal effort waste |

---

## 8. Summary — Coverage by Layer (%)

| Layer | Test Type | Coverage | Caveat |
|---|---|---|---|
| Unit | Pure Mockito | **~90%** of service methods | ✅ Well covered by recent effort |
| Repository | @DataJpaTest (custom methods) | **70%** | 0% production-DB fidelity (H2 vs MySQL) |
| Controller | @WebMvcTest slices | **50%** | 0% security tested (addFilters = false) |
| Full Stack | @SpringBootTest | **10%** of domains | 0% error-scenario, 0% WebSocket tested |
| Service Integration | Service + Repository wired | **0%** | Transactional/flush/cascade never verified |
| External APIs | WireMock | **0%** | Not configured |
| E2E | Full system | **0%** | No browser/API-driven end-to-end tests |
| Production DB | Testcontainers (MySQL) | **0%** | Not configured |

---

## 9. Recommended Priority Order

```
P0 (Fix first — highest risk)
  ├── @SpringBootTest for auth flow (register → activate → login → refresh → signout)
  ├── Enable security filters in at least one @WebMvcTest slice
  └── Add Testcontainers (MySQL) for all @DataJpaTest and @SpringBootTest

P1 (High coverage impact)
  ├── @SpringBootTest for Post, Comment, Message, Chat flows
  ├── @WebMvcTest for AuthController (with security filters enabled)
  └── Create separate integrationTest Gradle task

P2 (Medium)
  ├── @DataJpaTest for NotificationRepository and PhotoRepository
  ├── WebSocket integration tests
  └── @WebMvcTest for NotificationController, PhotoController, ReactionController

P3 (Nice to have)
  ├── Remaining @WebMvcTest slices (AdminController)
  ├── Service-layer integration tests (Service + Repository wired)
  ├── WireMock for email service
  └── Fill remaining partial repository method coverage
```
