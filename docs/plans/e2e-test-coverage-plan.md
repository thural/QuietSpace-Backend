# E2E Test Coverage Plan — quietspace-backend

> **Based on:** E2E coverage gap analysis (July 2026)  
> **Goal:** Lift E2E (`@SpringBootTest` + Testcontainers) coverage to 100% of controllers with meaningful flow tests, closing all gaps identified in the assessment  
> **Target pyramid:** ≈70% unit / ≈20% integration / ≈10% E2E  
> **Strategy:** P0 → P1 → P2 (zero-coverage controllers first, then extend partial ones, then WebSocket)

---

## Table of Contents

- [Before You Start](#before-you-start)
- [P0 — Zero-Coverage Controllers (must add first)](#p0--zero-coverage-controllers-must-add-first)
- [P1 — Extend Partial Coverage](#p1--extend-partial-coverage)
- [P2 — WebSocket E2E Coverage](#p2--websocket-e2e-coverage)
- [Validation & Handoff](#validation--handoff)

---

## Before You Start

### Read-This-First Conventions

1. **Follow every guideline in** `docs/test/integration-test-guidelines.md`.
2. **Commit after each numbered sub-step** (e.g., P0.1, P0.2, P1.1). Do not batch.
3. **File naming:** `*IT.java` for `@SpringBootTest` + Testcontainers E2E tests, `*Test.java` for slice tests.
4. **Existing patterns to follow** — every E2E test must use this template:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfig.class)
@ActiveProfiles("testcontainers")
class XxxFlowIT {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @MockitoBean private PhotoService photoService;
    // ...
}
```

5. **No `@DirtiesContext`.** Use `@BeforeEach deleteAll()` on every repository touched.
6. **No repository mocking in E2E tests.** Always hit the real MySQL container.
7. **Mock external-only services:** `PhotoService`, `EmailService` — use `@MockitoBean`.
8. **Test both happy paths and error paths** (200, 400, 401, 404) in every flow.

### Current State (before P0)

| Controller | REST Endpoints | E2E Tests | Coverage |
|---|---|---|---|
| AuthController | 6 | ✅ AuthFlowIT (13 tests) | Full |
| PostController | 13 | ✅ PostFlowIT (7 tests) | 7/13 endpoints |
| CommentController | 9 | ✅ CommentFlowIT (5 tests) | 5/9 endpoints |
| MessageController | 4 | ✅ MessageFlowIT (4 tests) | Full |
| ChatController | 6 REST + 6 WS | ✅ ChatFlowIT (5 tests) | 5/6 REST, 1/6 WS |
| WebSocket (public chat) | — | ✅ WebSocketFlowIT (1 test) | Public chat echo only |
| **UserController** | **12 REST + 2 WS** | **❌ None** | **0%** |
| **AdminController** | **3** | **❌ None** | **0%** |
| **PhotoController** | **3** | **❌ None** | **0%** |
| **NotificationController** | **6 REST + 1 WS** | **❌ None** | **0%** |
| **ReactionController** | **4** | **❌ None** | **0%** |

---

## P0 — Zero-Coverage Controllers (must add first)

### P0.1 — UserFlowIT.java (highest business value)

**File:** `src/test/java/dev/thural/quietspace/controller/UserFlowIT.java`

Cover the core social features of `UserController`:

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `searchUser_byUsername_shouldReturn200` | GET `/api/v1/users/search?username=...` | 200 + JSON body |
| 2 | `getUserById_givenExistingId_shouldReturn200` | GET `/api/v1/users/{id}` | 200 + user fields |
| 3 | `getUserById_givenNonExistentId_shouldReturn404` | GET `/api/v1/users/{id}` (random UUID) | 404 |
| 4 | `updateUser_givenValidRequest_shouldReturn200` | PATCH `/api/v1/users` | 200 |
| 5 | `getProfile_shouldReturn200` | GET `/api/v1/users/profile` | 200 |
| 6 | `toggleFollow_shouldReturn200` | POST `/api/v1/users/follow/{userId}/toggle-follow` | 200 |
| 7 | `getFollowers_shouldReturn200` | GET `/api/v1/users/{userId}/followers` | 200 |
| 8 | `getFollowings_shouldReturn200` | GET `/api/v1/users/{userId}/followings` | 200 |
| 9 | `blockUser_shouldReturn200` | POST `/api/v1/users/profile/block/{userId}` | 200 |
| 10 | `removeFollower_shouldReturn200` | POST `/api/v1/users/followers/remove/{userId}` | 200 |
| 11 | `updateProfileSettings_shouldReturn200` | PATCH `/api/v1/users/profile/settings` | 200 |
| 12 | `deleteUser_shouldReturn204` | DELETE `/api/v1/users/{userId}` | 204 |

**Mocks:** `PhotoService`, `NotificationService`, `SimpMessagingTemplate`

**Estimated test methods:** 12

---

### P0.2 — AdminFlowIT.java

**File:** `src/test/java/dev/thural/quietspace/controller/AdminFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `adminGreeting_shouldReturn200` | GET `/api/v1/admin` | 200 |
| 2 | `listUsers_shouldReturn200` | GET `/api/v1/admin/users` | 200 + paginated results |
| 3 | `deleteUser_shouldReturn200` | POST `/api/v1/admin/{userId}` | 200 |

**Mocks:** `UserService` (if needed), `SimpMessagingTemplate`, `NotificationService`

**Note:** `AdminController` uses `@PreAuthorize("hasRole('ADMIN')")` — tests must authenticate with an ADMIN user. Use `helper.registerAndLoginAdmin(...)` from `IntegrationTestHelper`.

**Estimated test methods:** 3

---

### P0.3 — PhotoFlowIT.java

**File:** `src/test/java/dev/thural/quietspace/controller/PhotoFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `uploadProfilePhoto_shouldReturn200` | POST `/api/v1/photos/profile` (multipart) | 200 + photo name |
| 2 | `getPhoto_byName_shouldReturn200` | GET `/api/v1/photos/{name}` | 200 + image bytes |
| 3 | `getPhoto_byNonExistentName_shouldReturn404` | GET `/api/v1/photos/{name}` (random) | 404 |
| 4 | `deleteProfilePhoto_shouldReturn200` | DELETE `/api/v1/photos/profile/{userId}` | 200 |

**Mocks:** `PhotoService` **(must be real — undo the `@MockitoBean` for photo E2E)**
**Important:** For this flow test, `PhotoService` should NOT be mocked because we are testing actual file upload/download. Remove `@MockitoBean PhotoService` and instead let the real `PhotoService` interact with the filesystem (or Testcontainers volume). If the real `PhotoService` depends on cloud storage, use WireMock.

**Alternative:** If `PhotoService` cannot run locally, keep it mocked and test only the controller layer. In that case, verify the correct service method was called via `verify()`.

**Estimated test methods:** 4

---

### P0.4 — NotificationFlowIT.java

**File:** `src/test/java/dev/thural/quietspace/controller/NotificationFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `getNotifications_shouldReturn200` | GET `/api/v1/notifications` | 200 + paginated |
| 2 | `getNotifications_byType_shouldReturn200` | GET `/api/v1/notifications/type/{type}` | 200 |
| 3 | `countPendingNotifications_shouldReturn200` | GET `/api/v1/notifications/count-pending` | 200 + count |
| 4 | `markNotificationAsSeen_shouldReturn200` | POST `/api/v1/notifications/seen/{contentId}` | 200 |
| 5 | `processNotification_shouldReturn200` | POST `/api/v1/notifications/process` | 200 |

**Mocks:** `PhotoService`, `SimpMessagingTemplate`

**Estimated test methods:** 5

---

### P0.5 — ReactionFlowIT.java

**File:** `src/test/java/dev/thural/quietspace/controller/ReactionFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `toggleReaction_shouldReturn200` | POST `/api/v1/reactions/toggle-reaction` | 200 |
| 2 | `getReactions_byUser_shouldReturn200` | GET `/api/v1/reactions/user` | 200 |
| 3 | `getReactions_byContent_shouldReturn200` | GET `/api/v1/reactions/content` | 200 |
| 4 | `countReactions_shouldReturn200` | GET `/api/v1/reactions/count` | 200 + count |

**Mocks:** `PhotoService`, `NotificationService`, `SimpMessagingTemplate`

**Estimated test methods:** 4

---

## P1 — Extend Partial Coverage

### P1.1 — Extend PostFlowIT.java

Add tests for uncovered `PostController` endpoints.

**File:** `src/test/java/dev/thural/quietspace/controller/PostFlowIT.java` (existing)

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `searchPosts_byQuery_shouldReturn200` | GET `/api/v1/posts/search?query=...` | 200 + results |
| 2 | `getPosts_byUser_shouldReturn200` | GET `/api/v1/posts/user/{userId}` | 200 |
| 3 | `getCommentedPosts_shouldReturn200` | GET `/api/v1/posts/user/{userId}/commented` | 200 |
| 4 | `savePost_shouldReturn200` | PATCH `/api/v1/posts/saved/{postId}` | 200 |
| 5 | `getSavedPosts_shouldReturn200` | GET `/api/v1/posts/saved` | 200 |
| 6 | `repost_shouldReturn200` | POST `/api/v1/posts/repost` | 200 |
| 7 | `votePoll_shouldReturn200` | POST `/api/v1/posts/vote-poll` | 200 |

**Estimated additional test methods:** 7

---

### P1.2 — Extend CommentFlowIT.java

Add tests for uncovered `CommentController` endpoints.

**File:** `src/test/java/dev/thural/quietspace/controller/CommentFlowIT.java` (existing)

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `getReplies_toComment_shouldReturn200` | GET `/api/v1/comments/{commentId}/replies` | 200 |
| 2 | `getLatestComment_byUserOnPost_shouldReturn200` | GET `/api/v1/comments/user/{userId}/post/{postId}/latest` | 200 |

**Estimated additional test methods:** 2

---

### P1.3 — Extend ChatFlowIT.java (REST)

Add tests for uncovered `ChatController` REST endpoints.

**File:** `src/test/java/dev/thural/quietspace/controller/ChatFlowIT.java` (existing)

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `addMember_toChat_shouldReturn200` | PATCH `/api/v1/chats/{chatId}/members/add/{userId}` | 200 |
| 2 | `removeMember_fromChat_shouldReturn200` | PATCH `/api/v1/chats/{chatId}/members/remove/{userId}` | 200 |

**Estimated additional test methods:** 2

---

## P2 — WebSocket E2E Coverage

### P2.1 — Extend WebSocketFlowIT.java (private chat)

**File:** `src/test/java/dev/thural/quietspace/websocket/WebSocketFlowIT.java` (existing)

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `sendPrivateMessage_shouldDeliver` | STOMP `/app/private/chat` → recipient receives | Message delivered |
| 2 | `deleteMessage_shouldNotifyParticipants` | STOMP `/app/private/chat/delete/{id}` → DELETE_MESSAGE event | Event received |
| 3 | `markMessageAsSeen_shouldNotifyParticipants` | STOMP `/app/private/chat/seen/{id}` → SEEN_MESSAGE event | Event received |
| 4 | `leaveChat_shouldNotifyParticipants` | STOMP `/app/private/chat/leave` → LEFT_CHAT event | Event received |
| 5 | `joinChat_shouldNotifyParticipants` | STOMP `/app/private/chat/join` → JOINED_CHAT event | Event received |

**Estimated additional test methods:** 5

---

### P2.2 — NotificationWebSocketFlowIT.java

**File:** `src/test/java/dev/thural/quietspace/websocket/NotificationWebSocketFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `markNotificationSeen_viaWebSocket_shouldWork` | STOMP `/app/private/notifications/seen/{id}` | 200 |

**Estimated test methods:** 1

---

### P2.3 — UserWebSocketFlowIT.java (online status)

**File:** `src/test/java/dev/thural/quietspace/websocket/UserWebSocketFlowIT.java`

| # | Test | Scenario | Assert |
|---|---|---|---|
| 1 | `setOnlineStatus_shouldBroadcast` | STOMP `/app/user/setOnlineStatus` → `/user/public` | Status broadcast |
| 2 | `getOnlineUsers_shouldReturnList` | STOMP `/app/user/onlineUsers` | Online users list |

**Estimated test methods:** 2

---

## Validation & Handoff

### Final checklist

```bash
# 1. Compile everything
./gradlew compileTestJava

# 2. Run full test suite
./gradlew clean test integrationTest

# 3. Generate coverage report
./gradlew jacocoTestReport

# 4. Verify coverage distribution
#    Unit: ~70% of tests
#    Integration (slice + repo): ~20% of tests
#    E2E (@SpringBootTest + Testcontainers): ~10% of tests
#    Every controller has at least one *FlowIT.java E2E test
```

### Summary of Deliverables

| Tier | New Files | New Test Methods | Cumulative |
|---|---|---|---|
| P0.1 (UserFlowIT) | 1 | 12 | 12 |
| P0.2 (AdminFlowIT) | 1 | 3 | 15 |
| P0.3 (PhotoFlowIT) | 1 | 4 | 19 |
| P0.4 (NotificationFlowIT) | 1 | 5 | 24 |
| P0.5 (ReactionFlowIT) | 1 | 4 | 28 |
| P1.1 (Extend PostFlowIT) | 0 | 7 | 35 |
| P1.2 (Extend CommentFlowIT) | 0 | 2 | 37 |
| P1.3 (Extend ChatFlowIT REST) | 0 | 2 | 39 |
| P2.1 (Extend WebSocketFlowIT) | 0 | 5 | 44 |
| P2.2 (NotificationWSFlowIT) | 1 | 1 | 45 |
| P2.3 (UserWSFlowIT) | 1 | 2 | 47 |
| **Total** | **7 new files** | **~47 tests** | |

### Total Effort Estimate

| Tier | Relative Effort |
|---|---|
| P0.1 (UserFlowIT) | Large (~12 tests, complex social flows) |
| P0.2–P0.3 | Small (~3–4 tests each) |
| P0.4–P0.5 | Small (~4–5 tests each) |
| P1.1 (Extend PostFlowIT) | Medium (~7 tests) |
| P1.2–P1.3 | Small (~2 tests each) |
| P2.1 (WebSocket private chat) | Medium (~5 tests, complex WS setup) |
| P2.2–P2.3 | Small (~1–2 tests each) |
| **Total** | **~2–3 days** |
