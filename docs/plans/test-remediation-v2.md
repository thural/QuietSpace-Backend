# Phase 2 Test Remediation Plan: Systemic Root Cause Resolution

**Date:** 2026-07-11
**Objective:** Resolve the remaining 135 test failures by systematically addressing transactional issues, entity validation errors, and context load failures.

---

## Remediation Phases

### Phase 1: Transactional Integrity (`TransactionRequiredException`)
**Context**: Most `*FlowIT` tests fail with `TransactionRequiredException` because they modify data (e.g., `save`, `delete`) outside of an active transaction.

1.  **Action**: Annotate all `dev.thural.quietspace.controller.*FlowIT` and `dev.thural.quietspace.service.*IT` classes with `@Transactional`.
2.  **Verification**: Execute `*FlowIT` test suites.

### Phase 2: Entity Validation (`ConstraintViolationException`)
**Context**: Repository tests fail because test data initialization does not satisfy JPA/Jakarta validation constraints.

1.  **Action**: Update `*RepositoryTest` setup methods to ensure all `@NotNull`, `@NotBlank`, and other mandatory fields are populated.
2.  **Action**: Standardize test data creation by introducing a helper utility `TestEntityFactory` to ensure entities are created in a valid state.
3.  **Verification**: Execute `*RepositoryTest` suites.

### Phase 3: Context Stability (`IllegalStateException`)
**Context**: Controller tests are failing to load the `ApplicationContext` due to inconsistent usage of `@WebMvcTest` versus `MockMvcBuilders.standaloneSetup()`.

1.  **Action**: Standardize controller slice tests to use `@WebMvcTest`. Ensure all service dependencies are explicitly mocked via `@MockitoBean`.
2.  **Action**: For tests that must use `standaloneSetup`, configure `MappingJackson2HttpMessageConverter` with a correctly initialized `ObjectMapper` (including Spring Data support).
3.  **Verification**: Execute all `*ControllerTest` classes.

### Phase 4: Final Validation
1.  **Action**: Run full test suite: `./gradlew test`.
2.  **Action**: If failures persist, generate a consolidated failure report for the final set of edge cases.

---

## Execution Checklist

- [ ] Apply `@Transactional` to `*FlowIT` classes
- [ ] Audit and fix `*RepositoryTest` data initialization
- [ ] Implement `TestEntityFactory` for repository tests
- [ ] Standardize controller unit/slice tests (`@WebMvcTest`)
- [ ] Execute full `gradlew test`
- [ ] Generate final failure report
