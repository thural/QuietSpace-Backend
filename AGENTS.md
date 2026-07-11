# AGENTS.md — Session Summary

## Objective
Implement E2E test coverage plan from `docs/plans/e2e-test-coverage-plan.md` — lift all controllers to meaningful `@SpringBootTest` + Testcontainers E2E coverage, committing after each sub-step.

## Important Details
- All E2E tests follow the template: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Import(TestcontainersConfig.class)` + `@ActiveProfiles("testcontainers")` + `@MockitoBean` for external services
- `PhotoService` runs **unmocked** in `PhotoFlowIT` because it stores images in DB (no filesystem/cloud dependency)
- `SimpMessagingTemplate` mocked via `@MockitoBean` in tests where it's an indirect dependency (UserController, AdminController, NotificationController, ReactionController)
- Existing slice tests use non-standard packages (`org.springframework.boot.webmvc.test.autoconfigure.*` for `@WebMvcTest`, `org.springframework.boot.data.jpa.test.autoconfigure.*` for `@DataJpaTest`)
- Two pre-existing gaps closed before P0: `QuietspaceApplicationTests` renamed to `QuietspaceApplicationIT` (moved to `integrationTest` task); `UserControllerIT.java` deleted (HTTP tests covered by `controller/slice/UserControllerTest.java`, validation-error tests added)

## Completed
- Gap 1: `QuietspaceApplicationTests.java` → `QuietspaceApplicationIT.java` (class rename + file rename, commit `45076ae`)
- Gap 2: Added `updateUser_givenTooLongUsername_shouldReturn400` and `updateUser_givenTooLongPassword_shouldReturn400` to `controller/slice/UserControllerTest.java` (commit `60bf99a`)
- Gap 2: Deleted legacy `controller/UserControllerIT.java` (commit `87ce706`)
- P0.1: `UserFlowIT.java` — 12 tests (search, getById, update, profile, follow, followers/followings, block, remove-follower, settings, delete) (commit `416ef93`)
- P0.2: `AdminFlowIT.java` — 4 tests (greeting, delete-user, list-users, non-admin-403) (commit `b56380c`)
- P0.3: `PhotoFlowIT.java` — 4 tests (upload-201, unsupported-type-400, non-existent-404, delete-204); `PhotoService` unmocked (commit `5c5a6d1`)
- P0.4: `NotificationFlowIT.java` — 5 tests (list-all, filter-by-type, count-pending, mark-seen, process); seeds a FOLLOW_REQUEST notification via follow action (commit `0657580`)
- P0.5: `ReactionFlowIT.java` — 4 tests (toggle-reaction, get-by-user, get-by-content, count); seeds a post via REST API then reacts to it (commit `d0a99e6`)
- P1.1: `PostFlowIT.java` extended — 7 additional tests (search, user-posts, commented-posts, save-post, get-saved-posts, repost, vote-poll) (commit `ff5db9b`)
- P1.2: `CommentFlowIT.java` extended — 2 additional tests (get-replies, get-latest-comment-by-user-on-post) (commit `35fd109`)
- P1.3: `ChatFlowIT.java` extended — 2 additional tests (add-member, remove-member) (commit `e1ef6e1`)
- P2.1: `WebSocketFlowIT.java` extended — 5 additional WebSocket tests (send-private-message, delete-message, mark-message-seen, leave-chat, join-chat) (commit `34118b8`)
- `docs/plans/e2e-test-coverage-plan.md`: E2E coverage plan covering all P0-P2 tiers
- `docs/tests/integration-test-guidelines.md`: integration testing guidelines matching codebase patterns

## Blockers
- Testcontainers E2E tests cannot run in the current environment (Docker connectivity issue from JVM — `DockerClientProviderStrategy.java:274`). All `@SpringBootTest` + Testcontainers tests fail to start the application context.
- Pre-existing failures in `controller/slice/*` tests (NPE at `UserControllerTest.java:147`, `CommentControllerTest.java:181`, etc.) — unrelated to these changes.

## Relevant Files
- `docs/plans/e2e-test-coverage-plan.md`: source E2E coverage plan (7 new files, ~47 tests total)
- `docs/tests/integration-test-guidelines.md`: integration testing best practices for the codebase
- `src/test/java/dev/thural/quietspace/controller/UserFlowIT.java`: 12 E2E tests (P0.1)
- `src/test/java/dev/thural/quietspace/controller/AdminFlowIT.java`: 4 E2E tests (P0.2)
- `src/test/java/dev/thural/quietspace/controller/PhotoFlowIT.java`: 4 E2E tests, PhotoService unmocked (P0.3)
- `src/test/java/dev/thural/quietspace/controller/NotificationFlowIT.java`: 5 E2E tests (P0.4)
- `src/test/java/dev/thural/quietspace/controller/ReactionFlowIT.java`: 4 E2E tests (P0.5)
- `src/test/java/dev/thural/quietspace/controller/PostFlowIT.java`: 14 tests (7 original + 7 added in P1.1)
- `src/test/java/dev/thural/quietspace/controller/CommentFlowIT.java`: 7 tests (5 original + 2 added in P1.2)
- `src/test/java/dev/thural/quietspace/controller/ChatFlowIT.java`: 7 tests (5 original + 2 added in P1.3)
- `src/test/java/dev/thural/quietspace/websocket/WebSocketFlowIT.java`: 6 tests (1 original + 5 added in P2.1)
- `src/test/java/dev/thural/quietspace/QuietspaceApplicationIT.java`: renamed from `QuietspaceApplicationTests.java` (Gap 1 fix)
