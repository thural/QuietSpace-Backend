# Unit Test Coverage Assessment — QuietSpace-Backend

> **Date:** 2026-07-11  
> **Project:** QuietSpace-Backend (Java Spring Boot)  
> **Build Tool:** Gradle (Kotlin DSL)  
> **Testing Stack:** JUnit 5, Mockito, AssertJ, Spring Boot Test, JaCoCo  

---

## Overall Instruction Coverage (JaCoCo): **40%**

This figure includes all test types (unit, slice, integration). Pure unit tests (standalone Mockito, no Spring context) account for a smaller share.

---

## Coverage by Package

| Package | Instruction Coverage | Status |
|---|---|---|
| `enums` | 92% | **Good** |
| `mapper` | 82% | **Good** |
| `bootstrap` | 76% | **Good** (indirect) |
| `config` | 74% | **Adequate** |
| `query` | 72% | **Adequate** |
| `entity` | 47% | Partial |
| `service.impl` | 36% | **Low** |
| `controller` | 32% | **Low** |
| `security` | 33% | **Low** |
| `utils` | 23% | **Low** |
| `exception` | 8% | **Very Low** |
| `authentication.service` | 0% | **Untested** |
| `authentication.controller` | 0% | **Untested** |
| `model.response` | 0% | Untested |
| `repository.specifications` | 0% | Untested |
| `websocket.*` | 5–49% | **Very Low** |

---

## Service Layer — Unit Test Breakdown

**6 of 11 service implementations have test classes (55%). Coverage within tested classes is incomplete.**

| Service Class | Methods Tested | Total Public Methods | Method Coverage | Notes |
|---|---|---|---|---|
| `ReactionServiceImpl` | 6 | 6 | **100%** | All public methods covered |
| `CommentServiceImpl` | 8 | 9 | **89%** | Missing `getLatestCommentByUserIdAndPostId` |
| `ChatServiceImpl` | 6 | 7 | **86%** | Missing `getChatById` |
| `MessageServiceImpl` | 4 | 6 | **67%** | Missing `setMessageSeen`, `getMessageById` |
| `PostServiceImpl` | 8 | 14 | **57%** | Missing `patchPost`, `getAllByQuery`, `addRepost`, `getSavedPostsByUser`, `savePostForUser`, `getCommentedPostsByUserId` |
| `UserServiceImpl` | 8 | 18 | **44%** | Missing `queryUsers`, `listUsersByUsername`, `listFollowings`, `listFollowers`, `toggleFollow`, `removeFollower`, `setOnlineStatus`, `findConnectedFollowings`, `saveProfileSettings`, `addUserToBlockList` |
| **`AuthService`** | **0** | **7** | **0%** | **Registration, login, token refresh, account activation, signout all untested** |
| **`PhotoServiceImpl`** | **0** | **8** | **0%** | **Upload, compression, retrieval, deletion all untested** |
| **`NotificationServiceImpl`** | **0** | **6** | **0%** | **Real-time notifications, WebSocket delivery untested** |
| **`EmailService`** | **0** | **1** | **0%** | **Email sending logic untested** |
| **`CommonServiceImpl`** | **0** | **1** | **0%** | `getSignedUser` untested |
| **TOTAL (service impl)** | **40** | **83** | **48%** | |

---

## Controller Layer — Test Coverage

**5 of 10 controllers have tests (unit + `@WebMvcTest` slice). 5 have zero tests.**

| Controller | Unit Test | Slice (`@WebMvcTest`) | Integration (`@SpringBootTest`) |
|---|---|---|---|
| `UserController` | Yes | Yes | Yes |
| `PostController` | Yes | Yes | No |
| `CommentController` | Yes | Yes | No |
| `MessageController` | Yes | Yes | No |
| `ChatController` | Yes | Yes | No |
| **`AuthController`** | **No** | **No** | **No** |
| **`AdminController`** | **No** | **No** | **No** |
| **`PhotoController`** | **No** | **No** | **No** |
| **`NotificationController`** | **No** | **No** | **No** |
| **`ReactionController`** | **No** | **No** | **No** |

---

## Security Layer — Zero Direct Tests

No dedicated unit tests exist for any security class. These are exercised only indirectly through `@WebMvcTest` slice tests:

| Class | Direct Unit Tests |
|---|---|
| `JwtService` | None |
| `JwtFilter` | None |
| `SecurityConfig` | None |
| `CustomAccessDeniedHandler` | None |
| `JwtAuthEntryPoint` | None |
| `SecurityErrorHandler` | None |

---

## Utility Layer — Zero Dedicated Tests

| Utility Class | Tests | Notes |
|---|---|---|
| `PagingProvider` | None | Builds `PageRequest`, sort constants |
| `PageUtils` | None | Converts `List<T>` to `Page<T>` |
| `ImageCompressionUtil` | None | Image compress/decompress via Thumbnailator |

---

## Repository Layer — Adequate

7 of 9 repositories have `@DataJpaTest` test classes:

| Repository | Has Tests |
|---|---|
| `UserRepository` | Yes |
| `PostRepository` | Yes |
| `CommentRepository` | Yes |
| `MessageRepository` | Yes |
| `ChatRepository` | Yes |
| `ReactionRepository` | Yes |
| `TokenRepository` | Yes |
| **`NotificationRepository`** | **No** |
| **`PhotoRepository`** | **No** |

---

## Mapper Layer — Strong (82%)

All 7 mappers have dedicated, thorough test classes covering edge cases:

| Mapper | Tests |
|---|---|
| `UserMapperTest` | Yes (comprehensive) |
| `PostMapperTest` | Yes (comprehensive) |
| `CommentMapperTest` | Yes (comprehensive) |
| `ReactionMapperTest` | Yes (comprehensive) |
| `MessageMapperTest` | Yes (comprehensive) |
| `ChatMapperTest` | Yes (comprehensive) |
| `NotificationMapperTest` | Yes (comprehensive) |

---

## Additional Untested Areas

| Area | Classes | Impact |
|---|---|---|
| **WebSocket** | `CustomHandshakeHandler`, `CustomHandshakeInterceptor`, `StompPrincipal`, `WebSocketConfig`, `WebSocketSecurityConfig`, `SocketEventListener`, event models | Real-time messaging, notification delivery |
| **Exceptions / Error Handling** | `GlobalExceptionHandler`, `MessagingExceptionHandler`, all custom exception types | Error handling |
| **Specifications** | `PostSpecifications` | Dynamic query building |
| **Query** | `UserQuery` | Custom user search queries |
| **Bootstrap** | `AdminLoader`, `TokenCleaner` | Startup logic, scheduled cleanup |

---

## Test Execution Status

**256 tests completed, 57 failed** in the latest run. This indicates existing tests (primarily integration/slice tests with Spring context dependencies) have environmental or configuration issues.

---

## Summary of Key Gaps (Priority Order)

1. **AuthService** (registration, login, token refresh, activation) — **0% coverage, highest priority**
2. **PhotoService** (upload, compression, retrieval) — **0% coverage**
3. **NotificationService** (real-time notifications) — **0% coverage**
4. **5 controllers** (Auth, Admin, Photo, Notification, Reaction) — **0% coverage**
5. **Security layer** (JWT, filters, access denial) — no dedicated unit tests
6. **Utilities** (`ImageCompressionUtil`, `PageUtils`, `PagingProvider`) — no direct tests
7. **Even tested services have significant holes** — e.g., `UserServiceImpl` covers only 8 of 18 methods

---

## Against the Testing Pyramid Standard

| Layer | Recommended | Current (Estimated) |
|---|---|---|
| Unit Tests | ~70% of suite | ~30–40% instruction coverage |
| Integration Tests | ~20% of suite | ~15–20% instruction (slice + data jpa) |
| End-to-End Tests | ~10% of suite | Minimal |
| **Total Instruction Coverage** | **~70% target** | **40% actual** |

To reach the standard, effort should focus on:
- Unit tests for `AuthService`, `PhotoService`, `NotificationService`, `EmailService`, `CommonServiceImpl`
- Completing partial coverage in `UserServiceImpl` and `PostServiceImpl`
- Adding controller tests for the 5 missing controllers
- Adding security-layer unit tests
- Adding utility-layer unit tests
- Fixing the 57 failing tests
