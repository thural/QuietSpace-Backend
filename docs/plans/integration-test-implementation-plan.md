# Integration Test Implementation Plan — QuietSpace-Backend

> **Based on:** `docs/reports/integration-test-assessment.md`  
> **Goal:** Lift integration coverage from current baseline to >= 70% across all layers  
> **Strategy:** P0 → P1 → P2 → P3 (highest risk first)

---

## Table of Contents

- [Before You Start](#before-you-start)
- [P0 — Critical (must fix first)](#p0--critical-must-fix-first)
- [P1 — High Coverage Impact](#p1--high-coverage-impact)
- [P2 — Medium](#p2--medium)
- [P3 — Nice to Have](#p3--nice-to-have)
- [Validation & Handoff](#validation--handoff)

---

## Before You Start

### Read-This-First Conventions

1. **Follow every guideline in** `docs/test/unit-test-guidelines.md` — they apply to integration tests too where relevant.
2. **Commit after each completed tier** (P0, P1, P2, P3). Do not batch everything into one commit.
3. **Integration test naming:** Files that use `@SpringBootTest` must end in `*IT.java` (e.g., `AuthFlowIT.java`). Files that use `@WebMvcTest` or `@DataJpaTest` stay as `*Test.java` (they are slice tests).
4. **Build task:** All integration/slice tests currently run under `gradle test`. Do NOT change this in P0–P2. The separate task is in P3.
5. **Never use @DirtiesContext.** Always clean state manually (`@AfterEach` cleanup or transactional rollback).
6. **Never mock a repository in an integration test.** If it's an integration test, let it touch the real database (or Testcontainers).

### Environment Setup

```bash
# You will need Docker running for Testcontainers (P0.3)
docker ps  # verify Docker is available

# PostgreSQL is used in the Testcontainers config.
# If you do not have PostgreSQL locally, Testcontainers handles it automatically.
```

### Relevant Source Directories

| Layer | Source | Test |
|---|---|---|
| Controllers | `src/main/java/.../controller/` | `src/test/java/.../controller/slice/` (for @WebMvcTest) |
| Controllers | `src/main/java/.../authentication/controller/` | `src/test/java/.../authentication/controller/` |
| Repositories | `src/main/java/.../repository/` | `src/test/java/.../repository/` |
| Full integration | — | `src/test/java/.../controller/` (for *IT.java) |

### Existing Test Files Reference

| File | What It Does |
|---|---|
| `controller/UserControllerIT.java` | Only existing @SpringBootTest. Tests user CRUD via MockMvc with H2. |
| `controller/slice/PostControllerTest.java` | @WebMvcTest for PostController (filters disabled). |
| `controller/slice/MessageControllerTest.java` | @WebMvcTest for MessageController (filters disabled). |
| `controller/slice/UserControllerTest.java` | @WebMvcTest for UserController (filters disabled). |
| `controller/slice/CommentControllerTest.java` | @WebMvcTest for CommentController (filters disabled). |
| `controller/slice/ChatControllerTest.java` | @WebMvcTest for ChatController (filters disabled). |
| `repository/*RepositoryTest.java` (7 files) | @DataJpaTest for each repository. All use H2. |

---

## P0 — Critical (Must Fix First)

### P0.1 — Testcontainers Setup

**Why:** All existing @DataJpaTest and @SpringBootTest tests run against H2. MySQL-specific behaviour is never verified. Testcontainers gives us a real MySQL container so tests match production.

**Files to create/modify:**

1. **`src/test/java/dev/thural/quietspace/config/TestcontainersConfig.java`** — Base config class:

```java
package dev.thural.quietspace.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfig {

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }
}
```

Note: `@ServiceConnection` (Spring Boot 3.1+) auto-configures the datasource from the container. If your Spring Boot version is older, you'll need `@DynamicPropertySource` instead.

2. **`src/test/resources/application-test.yml`** — Test profile properties:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
  flyway:
    enabled: false
```

3. **Update `application.yml` (test resources)** — Keep H2 as default but allow override:

```yaml
spring:
  profiles:
    active: test
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=user
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
    show-sql: false
  flyway:
    enabled: false
  sql:
    init:
      mode: never
```

**Key design decision:** Keep H2 as the default (so tests run without Docker), but provide Testcontainers as an opt-in via `@ActiveProfiles("testcontainers")` or by having specific IT tests import the config.

4. **Update `build.gradle.kts`** — Add required dependencies:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // Testcontainers
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:junit-jupiter")
}
```

**Validation:** Run `./gradlew test --tests "dev.thural.quietspace.config.TestcontainersConfig"` — should start a MySQL container and connect successfully.

---

### P0.2 — Auth Flow @SpringBootTest

**Why:** The most critical business flow (register → activate → login → JWT → refresh → signout) has zero full-stack integration coverage.

**File to create:** `src/test/java/dev/thural/quietspace/controller/AuthFlowIT.java`

**Test structure:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)  // or @ActiveProfiles("testcontainers")
class AuthFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        tokenRepository.deleteAll();
    }

    // 1. Register - success
    @Test
    void register_givenValidRequest_shouldReturn200WithAuthResponse() throws Exception { ... }

    // 2. Register - duplicate email
    @Test
    void register_givenExistingEmail_shouldReturn409() throws Exception { ... }

    // 3. Register - missing fields
    @Test
    void register_givenInvalidBody_shouldReturn400() throws Exception { ... }

    // 4. Activate account - valid token
    @Test
    void activateAccount_givenValidToken_shouldReturn200() throws Exception { ... }

    // 5. Activate account - invalid token
    @Test
    void activateAccount_givenInvalidToken_shouldReturn400() throws Exception { ... }

    // 6. Authenticate - valid credentials
    @Test
    void authenticate_givenValidCredentials_shouldReturn200WithJwt() throws Exception {
        // First register + activate the user, then login
    }

    // 7. Authenticate - wrong password
    @Test
    void authenticate_givenWrongPassword_shouldReturn401() throws Exception { ... }

    // 8. Authenticate - non-existent user
    @Test
    void authenticate_givenUnknownEmail_shouldReturn401() throws Exception { ... }

    // 9. Refresh token - valid
    @Test
    void refreshToken_givenValidToken_shouldReturnNewAccessToken() throws Exception { ... }

    // 10. Refresh token - expired/revoked
    @Test
    void refreshToken_givenExpiredToken_shouldReturn401() throws Exception { ... }

    // 11. Signout - valid bearer
    @Test
    void signout_givenValidBearer_shouldBlacklistTokenAndReturn200() throws Exception { ... }

    // 12. Access protected endpoint without token
    @Test
    void accessProtectedEndpoint_withoutToken_shouldReturn401() throws Exception { ... }

    // 13. Access protected endpoint with blacklisted token
    @Test
    void accessProtectedEndpoint_withBlacklistedToken_shouldReturn401() throws Exception { ... }
}
```

**Key things to test in the auth flow:**
- Full register → email token created → activate → login → JWT returned → access protected endpoint with JWT
- Token blacklisting on signout
- Refresh token rotation
- Account already activated (idempotency)
- Validation error responses (invalid email, short password, missing fields)

**Note on email:** The `EmailService` is `@Async`. In the integration test, either:
- Mock it with `@MockitoBean` (since we're testing the controller/auth-service, not the email sending), OR
- Use `Testcontainers` + `GreenMail` (if you want to verify email sending end-to-end)

Recommendation: Use `@MockitoBean` for `EmailService` in this IT, since email delivery is a separate concern.

---

### P0.3 — Enable Security Filters in One @WebMvcTest Slice

**Why:** Zero existing slice tests verify that `@PreAuthorize`, role checks, or JWT validation actually reject unauthorized requests.

**File to modify:** Pick one existing slice test (e.g., `controller/slice/PostControllerTest.java`) and add a variant without `addFilters = false`.

**Approach:** Either create a second test class, or add tests to the existing class but without disabling filters.

**Recommended:** Create `controller/slice/PostControllerSecurityTest.java`:

```java
@WebMvcTest(controllers = PostController.class)
// NOTE: no addFilters = false — security filters are active
class PostControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    // ... other mocks ...

    @Test
    void getPostById_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPost_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletePost_withUserRoleAndNotOwnPost_shouldReturn403() throws Exception {
        // Mock authentication with a user that is not the post author
        // Expect 403 Forbidden
    }
}
```

**Expand this pattern to at least one endpoint per controller** so each permission rule is covered.

---

## P1 — High Coverage Impact

### P1.1 — @SpringBootTest for Post/Comment/Message/Chat Flows

**Why:** After the auth flow, these are the next most critical domains. Zero end-to-end coverage exists for them.

**Files to create:**

1. **`src/test/java/dev/thural/quietspace/controller/PostFlowIT.java`**
2. **`src/test/java/dev/thural/quietspace/controller/CommentFlowIT.java`**
3. **`src/test/java/dev/thural/quietspace/controller/MessageFlowIT.java`**
4. **`src/test/java/dev/thural/quietspace/controller/ChatFlowIT.java`**

**Each file should cover:**
- **Happy path:** Create → read → update → delete
- **Error path:** Non-existent resource → 404, unauthorized access → 401/403, validation → 400
- **Paginated listing:** List all, filter by user, filter by search text
- **Edge cases:** Empty body, null IDs, constraint violations (text too long)
- **Authorization:** Own resource vs others' resources, admin access

**Example structure for `PostFlowIT.java`:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestcontainersConfig.class)
class PostFlowIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private PhotoService photoService;  // mock photo upload in integration tests

    private String jwtToken;
    private User savedUser;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        userRepository.deleteAll();
        // Register + login a test user to get JWT
        jwtToken = registerAndLoginTestUser();
    }

    @Test
    void createPost_givenValidRequest_shouldReturn201() throws Exception { ... }

    @Test
    void createPost_givenEmptyText_shouldReturn400() throws Exception { ... }

    @Test
    void getPostById_givenExistingId_shouldReturn200() throws Exception { ... }

    @Test
    void getPostById_givenNonExistentId_shouldReturn404() throws Exception { ... }

    @Test
    void updatePost_givenOwnPost_shouldReturn200() throws Exception { ... }

    @Test
    void updatePost_givenOtherUsersPost_shouldReturn403() throws Exception { ... }

    @Test
    void deletePost_givenOwnPost_shouldReturn204() throws Exception { ... }

    @Test
    void deletePost_givenOtherUsersPost_shouldReturn403() throws Exception { ... }

    @Test
    void getAllPosts_shouldReturnPagedResults() throws Exception { ... }

    @Test
    void getAllPosts_withPagination_shouldRespectPageSize() throws Exception { ... }

    @Test
    void searchPosts_withQuery_shouldReturnMatchingResults() throws Exception { ... }

    @Test
    void searchPosts_withEmptyQuery_shouldReturnAll() throws Exception { ... }
}
```

**Helper utility** — Extract the "register + login to get JWT" logic into a shared base class or utility to avoid duplication across IT files:

```java
// src/test/java/dev/thural/quietspace/utils/IntegrationTestHelper.java
@Component
public class IntegrationTestHelper {

    @Autowired
    private MockMvc mockMvc;

    public String registerAndLogin(String email, String password) throws Exception {
        // POST /api/v1/auth/register
        // POST /api/v1/auth/authenticate
        // Extract access_token from response
        // Return the token string
    }
}
```

---

### P1.2 — @WebMvcTest for AuthController

**Why:** The auth controller is the most security-sensitive endpoint. It needs a proper @WebMvcTest slice to verify Spring Security filter chain, validation, and response serialization.

**File to create:** `src/test/java/dev/thural/quietspace/controller/slice/AuthControllerTest.java`

```java
@WebMvcTest(controllers = AuthController.class)
// NOTE: no addFilters = false — security filters should be ACTIVE for auth
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_givenValidRequest_shouldReturn200() throws Exception {
        // Arrange
        var request = AuthRequest.builder().email("test@test.com").password("pass123").build();
        var response = AuthResponse.builder().accessToken("jwt").build();
        when(authService.register(any())).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"));
    }

    @Test
    void register_givenInvalidEmail_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_givenMissingBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ... more tests for authenticate, refresh, signout, activate ...
}
```

**Important:** The auth controller's `authenticate` and `register` endpoints are **public** (no auth required). Only `refresh`, `signout`, and protected endpoints require JWT. The @WebMvcTest should test both scenarios:
- Public endpoints (register, authenticate) work WITHOUT a token
- Protected endpoints return 401 WITHOUT a token

---

### P1.3 — Separate integrationTest Gradle Task

**Why:** Currently all tests run together. Developers need the ability to run only fast unit tests on every save.

**Modify `build.gradle.kts`:**

```kotlin
val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests only."
    group = "verification"
    
    useJUnitPlatform()
    
    filter {
        includeTestsMatching("*IT")
        includeTestsMatching("*ITCase")
    }
    
    shouldRunAfter(tasks.named("test"))
}

tasks.named("check") {
    dependsOn(integrationTest)
}
```

Then update test file naming:
- **Unit + slice tests:** Keep as `*Test.java` — run by `gradle test`
- **Full integration tests:** Rename to `*IT.java` — run by `gradle integrationTest`

Existing file to rename:
- `UserControllerIT.java` → already correctly named

**Note:** The `@WebMvcTest` and `@DataJpaTest` slice tests should remain in the `test` task since they are fast (no full context boot). Only `@SpringBootTest` full-stack tests use `*IT.java`.

---

## P2 — Medium

### P2.1 — @DataJpaTest for NotificationRepository and PhotoRepository

**Files to create:**

1. **`src/test/java/dev/thural/quietspace/repository/NotificationRepositoryTest.java`**

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private Notification savedNotification;

    @BeforeEach
    void setUp() { ... }

    @AfterEach
    void tearDown() { ... }

    @Test
    void findAllByUserId_shouldReturnNotifications() { ... }

    @Test
    void findAllByUserIdAndNotificationType_shouldFilterByType() { ... }

    @Test
    void findByContentIdAndUserId_shouldReturnNotification() { ... }

    @Test
    void countByUserIdAndIsSeen_shouldReturnCount() { ... }
}
```

2. **`src/test/java/dev/thural/quietspace/repository/PhotoRepositoryTest.java`**

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PhotoRepositoryTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Test
    void findByName_shouldReturnPhoto() { ... }

    @Test
    void findByEntityId_shouldReturnPhoto() { ... }

    @Test
    void deleteByEntityId_shouldRemovePhoto() { ... }

    @Test
    void findByName_givenNonExistentName_shouldReturnEmpty() { ... }
}
```

### P2.2 — WebSocket Integration Tests

**Why:** ChatController, NotificationController, and UserController publish STOMP events. These paths were identified as untested.

**File to create:** `src/test/java/dev/thural/quietspace/websocket/WebSocketFlowIT.java`

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
class WebSocketFlowIT {

    @Autowired
    private MockMvc mockMvc;

    private StompSession stompSession;

    @BeforeEach
    void connectWebSocket() throws Exception {
        // Register + login user
        // Connect to STOMP broker
        // Set up stompSession
    }

    @Test
    void sendChatMessage_overWebSocket_shouldDeliverToRecipient() throws Exception {
        // Subscribe to recipient's queue
        // Send message via STOMP
        // Assert recipient receives it
    }

    @Test
    void receiveNotification_overWebSocket_whenPostLiked() throws Exception {
        // Another user likes current user's post via REST
        // Assert current user receives WebSocket notification
    }
}
```

**Dependencies:** You may need `spring-boot-starter-websocket` test utilities. Add to `build.gradle.kts` if not present:

```kotlin
testImplementation("org.springframework.boot:spring-boot-starter-websocket")
```

### P2.3 — @WebMvcTest for NotificationController, PhotoController, ReactionController

**Why:** These controllers have only standalone MockMvc unit tests. They need proper @WebMvcTest slices to verify MVC infrastructure.

**Files to create:**

1. `src/test/java/dev/thural/quietspace/controller/slice/NotificationControllerTest.java`
2. `src/test/java/dev/thural/quietspace/controller/slice/PhotoControllerTest.java`
3. `src/test/java/dev/thural/quietspace/controller/slice/ReactionControllerTest.java`

Each should follow the same pattern as the existing slice tests (e.g., `PostControllerTest.java`), but without `addFilters = false` — or at minimum, have one test class variant with filters on.

---

## P3 — Nice to Have

### P3.1 — @WebMvcTest for AdminController

Low priority since admin endpoints are simple. Create if time permits.

### P3.2 — Service-Layer Integration Tests

**Why:** `@Transactional` rollback and Hibernate flush behaviour is never verified for service+repository interaction.

**File to create:** `src/test/java/dev/thural/quietspace/service/UserServiceIT.java`

```java
@SpringBootTest
@Import(TestcontainersConfig.class)
class UserServiceIT {

    @Autowired
    private UserService userService;  // REAL service, not mocked

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    void toggleFollow_shouldAddAndRemove_usingRealRepository() {
        // Create two users in DB
        // Follow -> verify followings list updated
        // Unfollow -> verify followings list empty
    }
}
```

### P3.3 — WireMock for Email Service

**Why:** The `EmailService` sends real emails via JavaMailSender. WireMock can mock the SMTP server so the email-sending path can be integration-tested without a real mail server.

**Add dependency:**
```kotlin
testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
// or
testImplementation("org.wiremock:wiremock-standalone:3.9.1")
```

**File to create:** `src/test/java/dev/thural/quietspace/service/EmailServiceIT.java`

### P3.4 — Fill Remaining Repository Method Coverage

- `PostRepository`: Add tests for `findSavedPostsByUserId`, `findByCommentsUserId`, `deleteByRepostId`
- `MessageRepository`: Add test for `findByMessageIdAndChatId`
- `CommentRepository`: Add test for `findLatestCommentByPostAndUserByUpdateDate`

These are quick additions to the existing @DataJpaTest files in `src/test/java/dev/thural/quietspace/repository/`.

---

## Validation & Handoff

### Before Each Commit

1. Run the specific tests you modified/created:
   ```bash
   ./gradlew test --tests "dev.thural.quietspace.controller.slice.AuthControllerTest"
   ```

2. Run the full test suite to check for regressions:
   ```bash
   ./gradlew test  # should not introduce NEW failures beyond the pre-existing 59
   ```

3. If you changed `build.gradle.kts`, verify the build compiles:
   ```bash
   ./gradlew compileTestJava
   ```

### Final Validation (end of P3)

```bash
./gradlew clean test integrationTest jacocoTestReport
```

Check the JaCoCo report at `build/reports/jacoco/test/html/index.html` — the overall instruction coverage should have increased from the 40% baseline.

### Commit Log Template

```
feat(test): P0.1 — add Testcontainers config for MySQL
feat(test): P0.2 — add AuthFlowIT (13 tests)
feat(test): P0.3 — add PostControllerSecurityTest (3 tests)
feat(test): P1.1 — add PostFlowIT, CommentFlowIT, MessageFlowIT, ChatFlowIT
feat(test): P1.2 — add @WebMvcTest for AuthController
feat(test): P1.3 — add integrationTest Gradle task
feat(test): P2.1 — add @DataJpaTest for NotificationRepository, PhotoRepository
feat(test): P2.2 — add WebSocketFlowIT
feat(test): P2.3 — add @WebMvcTest for NotificationController, PhotoController, ReactionController
feat(test): P3.1 — add @WebMvcTest for AdminController
feat(test): P3.2 — add service-layer integration tests
feat(test): P3.3 — add WireMock for EmailService
feat(test): P3.4 — fill remaining repository method coverage
```

### Estimated Effort

| Tier | Tests | Files | Est. Time |
|---|---|---|---|
| P0 | ~16 | 4 new + 2 modified | 3–4 hours |
| P1 | ~50 | 5 new + 1 modified | 4–6 hours |
| P2 | ~20 | 6 new | 3–4 hours |
| P3 | ~15 | 6 new + 4 modified | 2–3 hours |
| **Total** | **~100** | **21 new + 7 modified** | **12–17 hours** |
