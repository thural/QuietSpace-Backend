# Feature-Based Package Restructure Plan

## Objective

Migrate the current layered architecture to a **feature-based (package-by-feature)** layout, grouping all artifacts (controller, service, repository, entity, DTO, mapper) for each business capability into a single cohesive package. This lays the foundation for a modular monolith that can be cleanly decomposed into microservices.

---

## 1. Current State (Layered Architecture)

```
dev.thural.quietspace/
├── QuietspaceApplication.java
├── authentication/         ← partial feature (auth only)
│   ├── controller/AuthController.java
│   ├── service/AuthService.java
│   └── model/ (DTOs)
├── bootstrap/              ← OK (startup loaders)
├── config/                 ← OK (cross-cutting config)
├── controller/             ← ALL controllers (layered)
├── entity/                 ← ALL entities (layered)
├── enums/                  ← ALL enums (layered)
├── exception/              ← ALL exceptions + handlers + stray HelloController
├── mapper/                 ← ALL mappers (layered)
├── model/                  ← ALL DTOs (layered: request/ + response/)
├── query/                  ← single custom query class
├── repository/             ← ALL repositories + specifications (layered)
├── security/               ← OK (security infrastructure + JWT)
├── service/                ← ALL service interfaces + impl (layered)
├── utils/                  ← ALL utilities (layered)
└── websocket/              ← OK (websocket infrastructure)
```

### Problems
- No physical boundaries between business capabilities
- Cross-feature coupling via raw repository injection (e.g., `ChatController` directly injects `MessageRepository` and `UserRepository`)
- Entities, repositories, services, and DTOs for the same feature are scattered across 6+ top-level packages
- Extracting a microservice (e.g., `post`) would require hunting through every layered package

---

## 2. Target Structure (Feature-Based)

```
dev.thural.quietspace/
├── QuietspaceApplication.java
│
├── config/                        # Cross-cutting configuration
│   ├── AppConfig.java
│   ├── ApplicationAuditAware.java
│   ├── JpaAuditionConfiguration.java
│   ├── MailConfig.java
│   ├── OffsetDateTimeProvider.java
│   └── OpenApiConfig.java
│
├── shared/                        # Shared cross-feature components
│   ├── entity/
│   │   └── BaseEntity.java
│   ├── enums/
│   │   ├── EmailTemplateName.java
│   │   ├── EntityType.java
│   │   ├── EventType.java
│   │   ├── NotificationType.java
│   │   ├── Permission.java
│   │   ├── ReactionType.java
│   │   ├── Role.java
│   │   └── StatusType.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java       ← @ControllerAdvice
│   │   ├── MessagingExceptionHandler.java     ← @MessageExceptionHandler
│   │   ├── ActivationTokenException.java
│   │   ├── CustomDataNotFoundException.java
│   │   ├── CustomErrorException.java
│   │   ├── CustomMessagingException.java
│   │   ├── CustomParameterConstraintException.java
│   │   ├── CustomSocketException.java
│   │   ├── ImageUploadException.java
│   │   ├── OperationNotPermittedException.java
│   │   ├── UnauthorizedException.java
│   │   ├── UnsupportedImageTypeException.java
│   │   └── UserNotFoundException.java
│   ├── model/
│   │   ├── BaseResponse.java
│   │   ├── CustomErrorResponse.java
│   │   └── ErrorResponse.java
│   ├── service/
│   │   ├── CommonService.java
│   │   └── impl/
│   │       └── CommonServiceImpl.java
│   └── util/
│       ├── ImageCompressionUtil.java
│       ├── PageUtils.java
│       └── PagingProvider.java
│
├── security/                      # Security + JWT infrastructure
│   ├── SecurityConfig.java
│   ├── JwtService.java
│   ├── JwtFilter.java
│   ├── JwtAuthEntryPoint.java
│   ├── CustomAccessDeniedHandler.java
│   ├── SecurityErrorHandler.java
│   ├── Token.java                  ← moved from entity/
│   └── TokenRepository.java        ← moved from repository/
│
├── auth/                          # Authentication feature
│   ├── AuthController.java         ← moved from authentication/controller/
│   ├── AuthService.java            ← moved from authentication/service/
│   ├── dto/
│   │   ├── AuthRequest.java
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java
│   │   └── RegistrationRequest.java
│
├── user/                          # User feature
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserServiceImpl.java
│   ├── UserRepository.java
│   ├── UserQuery.java
│   ├── User.java
│   ├── ProfileSettings.java
│   ├── UserMapper.java
│   └── dto/
│       ├── UserRequest.java
│       ├── UserResponse.java
│       ├── ProfileSettingsRequest.java
│       └── ProfileSettingsResponse.java
│
├── post/                          # Post feature (+ polls)
│   ├── PostController.java
│   ├── PostService.java
│   ├── PostServiceImpl.java
│   ├── PostRepository.java
│   ├── PostSpecifications.java
│   ├── Post.java
│   ├── Poll.java
│   ├── PollOption.java
│   ├── PostMapper.java
│   └── dto/
│       ├── PostRequest.java
│       ├── PostResponse.java
│       ├── PollRequest.java
│       ├── PollResponse.java
│       ├── OptionResponse.java
│       ├── RepostRequest.java
│       └── VoteRequest.java
│
├── photo/                         # Photo/image feature
│   ├── PhotoController.java
│   ├── PhotoService.java
│   ├── PhotoServiceImpl.java
│   ├── PhotoRepository.java
│   ├── Photo.java
│   └── dto/
│       └── PhotoResponse.java
│
├── comment/                       # Comment feature
│   ├── CommentController.java
│   ├── CommentService.java
│   ├── CommentServiceImpl.java
│   ├── CommentRepository.java
│   ├── Comment.java
│   ├── CommentMapper.java
│   └── dto/
│       ├── CommentRequest.java
│       └── CommentResponse.java
│
├── reaction/                      # Reaction feature
│   ├── ReactionController.java
│   ├── ReactionService.java
│   ├── ReactionServiceImpl.java
│   ├── ReactionRepository.java
│   ├── Reaction.java
│   ├── ReactionMapper.java
│   └── dto/
│       ├── ReactionRequest.java
│       └── ReactionResponse.java
│
├── chat/                          # Chat feature
│   ├── ChatController.java
│   ├── ChatService.java
│   ├── ChatServiceImpl.java
│   ├── ChatRepository.java
│   ├── Chat.java
│   ├── ChatMapper.java
│   └── dto/
│       ├── CreateChatRequest.java
│       └── ChatResponse.java
│
├── message/                       # Message feature
│   ├── MessageController.java
│   ├── MessageService.java
│   ├── MessageServiceImpl.java
│   ├── MessageRepository.java
│   ├── Message.java
│   ├── MessageMapper.java
│   └── dto/
│       ├── MessageRequest.java
│       └── MessageResponse.java
│
├── notification/                  # Notification feature
│   ├── NotificationController.java
│   ├── NotificationService.java
│   ├── NotificationServiceImpl.java
│   ├── NotificationRepository.java
│   ├── Notification.java
│   ├── NotificationMapper.java
│   └── dto/
│       └── NotificationResponse.java
│
├── websocket/                     # WebSocket infrastructure
│   ├── config/
│   │   ├── WebSocketConfig.java
│   │   ├── WebSocketSecurityConfig.java
│   │   ├── CustomHandshakeHandler.java
│   │   ├── CustomHandshakeInterceptor.java
│   │   └── StompPrincipal.java
│   ├── event/
│   │   ├── listener/
│   │   │   └── SocketEventListener.java
│   │   └── message/
│   │       ├── BaseEvent.java
│   │       ├── ChatEvent.java
│   │       └── NotificationEvent.java
│   └── model/
│       └── UserRepresentation.java
│
├── bootstrap/                     # Startup loaders
│   ├── AdminLoader.java
│   └── TokenCleaner.java
```

**Test tree mirrors main** — each feature gets its own `test/` package with `*FlowIT.java`, `*ControllerTest.java`, and `*ServiceImplTest.java` as applicable.

---

## 3. Dependency Analysis (Migration Ordering)

Features sorted by **fewest external dependencies** (least-dependent first):

| Rank | Feature      | Depends On                          | Depended On By                         |
|------|-------------|-------------------------------------|----------------------------------------|
| 1    | `shared/`   | nothing (JDK + Spring)              | everything                             |
| 2    | `config/`   | shared (enums)                      | everything                             |
| 3    | `user/`     | shared, config                      | security, auth, post, photo, comment, reaction, chat, message, notification, websocket, bootstrap |
| 4    | `security/` | user, shared, config                | auth, websocket                        |
| 5    | `post/`     | user, shared, config                | photo, comment, reaction, notification |
| 6    | `auth/`     | user, security, shared, config      | (leaf — no feature depends on it)      |
| 7    | `chat/`     | user, shared, config                | message, notification                  |
| 8    | `photo/`    | user, post, shared, config          | user, post, message                    |
| 9    | `comment/`  | user, post, shared, config          | reaction, notification                 |
| 10   | `message/`  | user, chat, shared, config          | notification, chat (service level)     |
| 11   | `reaction/` | user, post/comment, shared, config  | (leaf)                                 |
| 12   | `notification/` | user, post, comment, message, shared, config | user controller, post controller, comment controller, reaction controller |
| 13   | `websocket/` | user, security, shared, config     | notification                           |
| 14   | `bootstrap/` | user, auth, shared, config         | (leaf)                                 |

**Rule**: A feature ranked N may reference features ranked 1..N-1 but never higher-ranked features. This avoids circular build breaks during migration.

---

## 4. Migration Phases

Each phase is atomic: after the phase, `./gradlew build` must pass.

---

### Phase 1: `shared/` Foundation

**What moves:**
| Source (current)                    | Target (new)                          | Notes                                |
|-------------------------------------|---------------------------------------|--------------------------------------|
| `enums/*.java` (8 files)           | `shared/enums/`                       | Update all imports across codebase   |
| `exception/*.java` (14 files)      | `shared/exception/`                   | Keep `GlobalExceptionHandler`, `MessagingExceptionHandler` |
| `utils/*.java` (3 files)           | `shared/util/`                        | `ImageCompressionUtil`, `PageUtils`, `PagingProvider` |
| `entity/BaseEntity.java`           | `shared/entity/`                      | All entities extend this             |
| `model/response/BaseResponse.java` | `shared/model/`                       | Abstract base for all response DTOs  |
| `model/response/CustomErrorResponse.java` | `shared/model/`               | Error response DTO                   |
| `model/response/ErrorResponse.java`       | `shared/model/`               | Error response DTO                   |
| `service/CommonService.java`       | `shared/service/`                     | Shared utility service               |
| `service/impl/CommonServiceImpl.java`     | `shared/service/impl/`         |                                     |

**Impact**: ~90 files across the codebase need import updates (enums and exceptions are widely referenced).

**Soundness**: After completion, confirm `./gradlew build` passes.

---

### Phase 2: `user/` Feature

**What moves:**
| Source                            | Target                              |
|-----------------------------------|-------------------------------------|
| `entity/User.java`                | `user/User.java`                    |
| `entity/ProfileSettings.java`     | `user/ProfileSettings.java`         |
| `repository/UserRepository.java`  | `user/UserRepository.java`          |
| `query/UserQuery.java`            | `user/UserQuery.java`               |
| `service/UserService.java`        | `user/UserService.java`             |
| `service/impl/UserServiceImpl.java` | `user/UserServiceImpl.java`        |
| `mapper/UserMapper.java`          | `user/UserMapper.java`              |
| `controller/UserController.java`  | `user/UserController.java`          |
| `model/request/UserRequest.java`  | `user/dto/UserRequest.java`         |
| `model/response/UserResponse.java`| `user/dto/UserResponse.java`        |
| `model/request/ProfileSettingsRequest.java` | `user/dto/ProfileSettingsRequest.java` |
| `model/response/ProfileSettingsResponse.java` | `user/dto/ProfileSettingsResponse.java` |

**Dependency note**: `UserServiceImpl` injects `PhotoService` (Phase 8). During this phase, `PhotoService` still lives in `service/` — the import is unchanged.

**Impact**: ~30 files need import updates.

---

### Phase 3: `security/` Enhancement (add Token)

**What moves:**
| Source                            | Target                              |
|-----------------------------------|-------------------------------------|
| `entity/Token.java`               | `security/Token.java`               |
| `repository/TokenRepository.java` | `security/TokenRepository.java`     |

**Impact**: ~5 files need import updates (`JwtFilter`, `AuthService`, `TokenCleaner`, test files).

**Note**: `security/` already exists — this phase just adds the token domain objects into it.

---

### Phase 4: `auth/` Feature

**What moves:**
| Source                                        | Target                              |
|-----------------------------------------------|-------------------------------------|
| `authentication/controller/AuthController.java` | `auth/AuthController.java`          |
| `authentication/service/AuthService.java`     | `auth/AuthService.java`             |
| `authentication/model/AuthRequest.java`       | `auth/dto/AuthRequest.java`         |
| `authentication/model/AuthResponse.java`      | `auth/dto/AuthResponse.java`        |
| `authentication/model/RegistrationRequest.java` | `auth/dto/RegistrationRequest.java` |
| `model/request/LoginRequest.java`             | `auth/dto/LoginRequest.java`        |

**Impact**: ~10 files need import updates.

---

### Phase 5: `post/` Feature (+ Poll)

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Post.java`                        | `post/Post.java`                    |
| `entity/Poll.java`                        | `post/Poll.java`                    |
| `entity/PollOption.java`                  | `post/PollOption.java`              |
| `repository/PostRepository.java`          | `post/PostRepository.java`          |
| `repository/specifications/PostSpecifications.java` | `post/PostSpecifications.java` |
| `service/PostService.java`                | `post/PostService.java`             |
| `service/impl/PostServiceImpl.java`       | `post/PostServiceImpl.java`         |
| `mapper/PostMapper.java`                  | `post/PostMapper.java`              |
| `controller/PostController.java`          | `post/PostController.java`          |
| `model/request/PostRequest.java`          | `post/dto/PostRequest.java`         |
| `model/response/PostResponse.java`        | `post/dto/PostResponse.java`        |
| `model/request/PollRequest.java`          | `post/dto/PollRequest.java`         |
| `model/response/PollResponse.java`        | `post/dto/PollResponse.java`        |
| `model/response/OptionResponse.java`      | `post/dto/OptionResponse.java`      |
| `model/request/RepostRequest.java`        | `post/dto/RepostRequest.java`       |
| `model/request/VoteRequest.java`          | `post/dto/VoteRequest.java`         |

**Impact**: ~25 files need import updates.

---

### Phase 6: `chat/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Chat.java`                        | `chat/Chat.java`                    |
| `repository/ChatRepository.java`          | `chat/ChatRepository.java`          |
| `service/ChatService.java`                | `chat/ChatService.java`             |
| `service/impl/ChatServiceImpl.java`       | `chat/ChatServiceImpl.java`         |
| `mapper/ChatMapper.java`                  | `chat/ChatMapper.java`              |
| `controller/ChatController.java`          | `chat/ChatController.java`          |
| `model/request/CreateChatRequest.java`    | `chat/dto/CreateChatRequest.java`   |
| `model/response/ChatResponse.java`        | `chat/dto/ChatResponse.java`        |

**Dependency note**: `ChatServiceImpl` injects `UserService` and `MessageRepository` (still in `service/` and `repository/` at this point). `ChatController` directly injects `MessageRepository` and `UserRepository` — **flag this anti-pattern** for post-migration refactoring.

**Impact**: ~15 files need import updates.

---

### Phase 7: `photo/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Photo.java`                       | `photo/Photo.java`                  |
| `repository/PhotoRepository.java`         | `photo/PhotoRepository.java`        |
| `service/PhotoService.java`               | `photo/PhotoService.java`           |
| `service/impl/PhotoServiceImpl.java`      | `photo/PhotoServiceImpl.java`       |
| `controller/PhotoController.java`         | `photo/PhotoController.java`        |
| `model/response/PhotoResponse.java`       | `photo/dto/PhotoResponse.java`      |

**Impact**: ~15 files need import updates.

---

### Phase 8: `comment/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Comment.java`                     | `comment/Comment.java`              |
| `repository/CommentRepository.java`       | `comment/CommentRepository.java`    |
| `service/CommentService.java`             | `comment/CommentService.java`       |
| `service/impl/CommentServiceImpl.java`    | `comment/CommentServiceImpl.java`   |
| `mapper/CommentMapper.java`               | `comment/CommentMapper.java`        |
| `controller/CommentController.java`       | `comment/CommentController.java`    |
| `model/request/CommentRequest.java`       | `comment/dto/CommentRequest.java`   |
| `model/response/CommentResponse.java`     | `comment/dto/CommentResponse.java`  |

**Impact**: ~15 files need import updates.

---

### Phase 9: `message/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Message.java`                     | `message/Message.java`              |
| `repository/MessageRepository.java`       | `message/MessageRepository.java`    |
| `service/MessageService.java`             | `message/MessageService.java`       |
| `service/impl/MessageServiceImpl.java`    | `message/MessageServiceImpl.java`   |
| `mapper/MessageMapper.java`               | `message/MessageMapper.java`        |
| `controller/MessageController.java`       | `message/MessageController.java`    |
| `model/request/MessageRequest.java`       | `message/dto/MessageRequest.java`   |
| `model/response/MessageResponse.java`     | `message/dto/MessageResponse.java`  |

**Impact**: ~15 files need import updates.

---

### Phase 10: `reaction/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Reaction.java`                    | `reaction/Reaction.java`            |
| `repository/ReactionRepository.java`      | `reaction/ReactionRepository.java`  |
| `service/ReactionService.java`            | `reaction/ReactionService.java`     |
| `service/impl/ReactionServiceImpl.java`   | `reaction/ReactionServiceImpl.java` |
| `mapper/ReactionMapper.java`              | `reaction/ReactionMapper.java`      |
| `controller/ReactionController.java`      | `reaction/ReactionController.java`  |
| `model/request/ReactionRequest.java`      | `reaction/dto/ReactionRequest.java` |
| `model/response/ReactionResponse.java`    | `reaction/dto/ReactionResponse.java`|

**Impact**: ~12 files need import updates.

---

### Phase 11: `notification/` Feature

**What moves:**
| Source                                    | Target                              |
|-------------------------------------------|-------------------------------------|
| `entity/Notification.java`                | `notification/Notification.java`    |
| `repository/NotificationRepository.java`  | `notification/NotificationRepository.java` |
| `service/NotificationService.java`        | `notification/NotificationService.java` |
| `service/impl/NotificationServiceImpl.java` | `notification/NotificationServiceImpl.java` |
| `mapper/NotificationMapper.java`          | `notification/NotificationMapper.java` |
| `controller/NotificationController.java`  | `notification/NotificationController.java` |
| `model/response/NotificationResponse.java`| `notification/dto/NotificationResponse.java` |

**Impact**: ~15 files need import updates.

---

### Phase 12: Cleanup & Test Migration

**Remove empty source packages:**
- `src/main/java/dev/thural/quietspace/enums/`
- `src/main/java/dev/thural/quietspace/exception/` (keep `HelloController.java` — relocate to an appropriate feature)
- `src/main/java/dev/thural/quietspace/entity/`
- `src/main/java/dev/thural/quietspace/repository/specifications/` (only `PostSpecifications` remains — already in `post/`)
- `src/main/java/dev/thural/quietspace/repository/`
- `src/main/java/dev/thural/quietspace/mapper/`
- `src/main/java/dev/thural/quietspace/model/request/`
- `src/main/java/dev/thural/quietspace/model/response/`
- `src/main/java/dev/thural/quietspace/model/`
- `src/main/java/dev/thural/quietspace/service/impl/`
- `src/main/java/dev/thural/quietspace/service/`
- `src/main/java/dev/thural/quietspace/utils/`
- `src/main/java/dev/thural/quietspace/query/`
- `src/main/java/dev/thural/quietspace/controller/`

**Relocate `HelloController.java`** — currently at `dev.thural.quietspace.exception.HelloController` (wrong package). Move to an appropriate feature (e.g., `controller.HelloController` or `user.HelloController`) or delete if it's debugging scaffolding.

**Mirror test structure**: Move test files to match main feature packages:
- `src/test/java/dev/thural/quietspace/controller/` → split into feature packages
- `src/test/java/dev/thural/quietspace/mapper/` → split into feature packages
- `src/test/java/dev/thural/quietspace/repository/` → split into feature packages
- `src/test/java/dev/thural/quietspace/service/` → split into feature packages
- `src/test/java/dev/thural/quietspace/service/impl/` → split into feature packages
- `src/test/java/dev/thural/quietspace/utils/` → `shared/util/`
- `src/test/java/dev/thural/quietspace/security/` → `security/`

---

## 5. Post-Migration Refactoring Checklist

Items to address after the structural move:

| # | Item | Priority | Description |
|---|------|----------|-------------|
| 1 | `ChatController` anti-pattern | **High** | Injects `MessageRepository` and `UserRepository` directly instead of through service layer. Extract into `ChatService` or `MessageService`. |
| 2 | Package-private visibility | **Medium** | After migration, change `UserRepository`, `PostRepository`, etc. to package-private (remove `public`) where possible. Other features must use the corresponding `*Service` interface. |
| 3 | Database isolation review | **Medium** | Audit `NotificationServiceImpl` — it directly queries `CommentRepository` and `PostRepository`. Consider introducing a read-only service interface or an event-driven pattern to avoid cross-feature repository access. |
| 4 | `UserService` ↔ `PhotoService` coupling | **Low** | `UserServiceImpl` injects `PhotoService`, and `PostServiceImpl` also injects `PhotoService`. This is acceptable (service-level dependency), but evaluate if a `Photo` is truly a user concern. |
| 5 | `WebSocketConfig` dependency density | **Low** | Injects `JwtService`, `UserDetailsService`, `UserRepository`. Evaluate if `UserRepository` should be behind a service interface. |

---

## 6. Execution Summary Table

| Phase | Description | Files Moved | Impact (files needing import updates) | Cumulative Build-After |
|-------|-------------|-------------|---------------------------------------|------------------------|
| 1     | `shared/` foundation | ~28 | ~90 | `./gradlew build` |
| 2     | `user/` feature | ~12 | ~30 | `./gradlew build` |
| 3     | `security/` + Token | ~2 | ~5 | `./gradlew build` |
| 4     | `auth/` feature | ~6 | ~10 | `./gradlew build` |
| 5     | `post/` feature | ~16 | ~25 | `./gradlew build` |
| 6     | `chat/` feature | ~8 | ~15 | `./gradlew build` |
| 7     | `photo/` feature | ~6 | ~15 | `./gradlew build` |
| 8     | `comment/` feature | ~8 | ~15 | `./gradlew build` |
| 9     | `message/` feature | ~8 | ~15 | `./gradlew build` |
| 10    | `reaction/` feature | ~8 | ~12 | `./gradlew build` |
| 11    | `notification/` feature | ~7 | ~15 | `./gradlew build` |
| 12    | Cleanup + test mirroring | ~176 test files | varies | `./gradlew build` |

**Total files moved**: ~113 main + ~176 test = ~289 files  
**Estimated import changes per phase**: 5–90 files

---

## 7. Risk Assessment

| Risk | Likelihood | Mitigation |
|------|-----------|------------|
| Missed imports cause compile failure | Medium | Phase-by-phase `./gradlew build` gates; IntelliJ "Optimize Imports" across project between phases |
| Circular dependency introduced | Low | Dependency ordering in Section 3 prevents this; verify with `jdeps` or Gradle `--scan` after Phase 12 |
| Test imports get stale | High | Mirror test file movement in the same phase as main file movement; use refactoring IDE tooling |
| `HelloController` cleanup breaks something | Low | Verify it's not referenced; delete or relocate in Phase 12 |

---

## 8. Tooling Recommendation

Use IntelliJ **Refactor → Move** on each package (or per-file) to automatically update `package` declarations and all import references. For batch operations:

```bash
# After each phase, run:
./gradlew build 2>&1 | grep "error:" | grep -oP "import dev\.thural\.quietspace\.\w+" | sort -u
```

This reveals stale imports that the IDE may have missed.

---

## Appendix: Entity → Feature Mapping

| Entity | Feature Package | Owner Feature |
|--------|----------------|---------------|
| `BaseEntity` | `shared/entity/` | shared base (all entities extend) |
| `User` | `user/` | user |
| `ProfileSettings` | `user/` | user |
| `Token` | `security/` | security |
| `Post` | `post/` | post |
| `Poll` | `post/` | post |
| `PollOption` | `post/` | post |
| `Photo` | `photo/` | photo |
| `Comment` | `comment/` | comment |
| `Reaction` | `reaction/` | reaction |
| `Chat` | `chat/` | chat |
| `Message` | `message/` | message |
| `Notification` | `notification/` | notification |

## Appendix: Service Interface → Feature Mapping

| Service Interface | Feature Package | Impl |
|------------------|----------------|------|
| `CommonService`  | `shared/service/` | `shared/service/impl/` |
| `UserService`    | `user/`        | `user/UserServiceImpl` |
| `PostService`    | `post/`        | `post/PostServiceImpl` |
| `PhotoService`   | `photo/`       | `photo/PhotoServiceImpl` |
| `CommentService` | `comment/`     | `comment/CommentServiceImpl` |
| `ReactionService`| `reaction/`    | `reaction/ReactionServiceImpl` |
| `ChatService`    | `chat/`        | `chat/ChatServiceImpl` |
| `MessageService` | `message/`     | `message/MessageServiceImpl` |
| `NotificationService` | `notification/` | `notification/NotificationServiceImpl` |
