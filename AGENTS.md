# AGENTS.md — Session Summary

## Objective
Implement E2E test coverage plan from `docs/plans/e2e-test-coverage-plan.md`, then remediate all test failures per `docs/plans/test-failure-remediation-plan.md`.

## Important Details
- All E2E tests follow the template: `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `@Import(TestcontainersConfig.class)` + `@ActiveProfiles("testcontainers")` + `@MockitoBean` for external services
- `PhotoService` runs **unmocked** in `PhotoFlowIT` because it stores images in DB (no filesystem/cloud dependency)
- `SimpMessagingTemplate` mocked via `@MockitoBean` in tests where it's an indirect dependency
- In Spring Boot 4.x, `@WebMvcTest` and `@AutoConfigureMockMvc` are at `org.springframework.boot.webmvc.test.autoconfigure.*` (split into `spring-boot-webmvc-test` module)
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` does **not** auto-configure MockMvc — must add `@AutoConfigureMockMvc`
- `@WebMvcTest` in 4.x does **not** auto-exclude `JwtFilter` — all slice tests must mock its dependencies (`TokenRepository`, `JwtService`, `UserDetailsService`)

## Completed
### E2E Coverage (P0–P2)
- Gap 1: `QuietspaceApplicationTests.java` → `QuietspaceApplicationIT.java` (commit `45076ae`)
- Gap 2: Validation tests + delete `UserControllerIT.java` (commits `60bf99a`, `87ce706`)
- P0.1–P0.5: UserFlowIT, AdminFlowIT, PhotoFlowIT, NotificationFlowIT, ReactionFlowIT (commits `416ef93`–`d0a99e6`)
- P1.1–P1.3: PostFlowIT, CommentFlowIT, ChatFlowIT extended (commits `ff5db9b`–`e1ef6e1`)
- P2.1: WebSocketFlowIT extended (commit `649a811`)
- `docs/plans/e2e-test-coverage-plan.md`, `docs/tests/integration-test-guidelines.md`

### Remediation
- Testcontainers `1.20.4` → `1.21.4` (build.gradle.kts)
- `@Captor` → inline `ArgumentCaptor.forClass()` in `UserControllerTest`, `CommentControllerTest`, `ChatControllerTest`
- `@WithUserDetails` → `@WithMockUser` in `CommentControllerTest`
- Added `@Valid` to `UserController.patchUser()`
- Fixed `patch(path, body)` → `patch(path)` bug in `UserControllerTest`
- Created `docs/plans/test-failure-remediation-plan.md`

## Active
- (none — awaiting user instruction to apply Tier 1–3 fixes)

## Blockers
- (none — Docker now reachable after Testcontainers upgrade; MockMvc fix is next)

## Current Test Status
- **513 total tests**
- **~151 failures** — 3 systemic root causes account for ~90%
- **Tier 1** (9 FlowIT + 6 slice + 4 JSON fix) would eliminate ~80 failures
- **Tier 4** (~30 scattered, lower priority) remain after systemic fixes

## Relevant Files
- `docs/plans/test-failure-remediation-plan.md`: step-by-step plan for remaining ~151 failures
- `docs/plans/e2e-test-coverage-plan.md`: original E2E coverage plan (P0-P2)
- `docs/tests/integration-test-guidelines.md`: testing guidelines (needs update per Section 2 of remediation plan)
- `build.gradle.kts`: Testcontainers upgraded to 1.21.4
- All `*FlowIT.java` files (9): need `@AutoConfigureMockMvc` (Tier 1.1)
- 6 slice test files: need `@MockitoBean` for JwtFilter deps (Tier 1.2)
- `ChatControllerTest.java`: needs mock response fields populated (Tier 1.3)
