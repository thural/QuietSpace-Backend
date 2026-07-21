# Security Remediation Implementation Plan

**Based on:** `docs/reports/security-analysis-report.md`  
**Approach:** Phased implementation from least-to-most global side effects  
**Goal:** Achieve production-ready security posture without cascading regressions

---

## Phase 0: Foundation & Safety Net (0 side effects)

**Goal:** Establish observability and rollback capability before any changes

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 0.1 | Add security-focused integration test suite | New: `*SecurityIT.java` | None | All existing tests pass + new tests fail (expected) |
| 0.2 | Enable `spring-boot-actuator` security audit endpoints | `application.yml` | None | `/actuator/health` shows details when authorized |
| 0.3 | Add dependency vulnerability scanning to CI | `.github/workflows/ci.yml` | None | Fail build on CVE ≥ HIGH |
| 0.4 | Document current CORS behavior in tests | Existing slice tests | None | Tests pass, baseline captured |

**Exit Criteria:** All existing tests pass; CI fails on new security tests (red phase)

---

## Phase 1: Logging & Observability Fixes (Localized, no behavior change)

**Goal:** Remove sensitive data from logs; add security event logging

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 1.1 | Remove JWT token logging in `AuthService.authenticate()` | `AuthService.java:73` | None | Logs show `[REDACTED]` instead of token |
| 1.2 | Remove activation code logging in `AuthService.generateAndSaveActivationToken()` | `AuthService.java:125` | None | Logs show `[REDACTED]` |
| 1.3 | Add structured security audit logging (login success/failure, token refresh, logout) | `AuthService.java`, `JwtFilter.java` | None | Audit log entries appear in structured format |
| 1.4 | Mask PII in `UserDetailsService` logging | `AppConfig.java:39` | None | Email/username masked in logs |

**Exit Criteria:** No secrets in logs; audit trail exists for auth events

---

## Phase 2: JWT Hardening (Isolated to token service)

**Goal:** Strengthen token validation without changing auth flow

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 2.1 | Add `issuer` claim validation in `JwtService` | `JwtService.java:83-90` | Low | Invalid issuer → 401; valid issuer → works |
| 2.2 | Add `audience` claim validation | `JwtService.java:83-90` | Low | Invalid aud → 401 |
| 2.3 | Add `jti` (JWT ID) claim to all tokens | `JwtService.java:51-68` | Low | Tokens contain unique `jti` |
| 2.4 | Store `jti` in `Token` entity for revocation tracking | `Token.java`, `TokenRepository.java` | Low | DB has `jti` column; queries work |
| 2.5 | Add token revocation check by `jti` in `JwtFilter` | `JwtFilter.java:42-50` | Low | Revoked token → 401; valid token → works |

**Exit Criteria:** All JWT validation tests pass; revoked tokens rejected

---

## Phase 3: Refresh Token Rotation (Auth service only)

**Goal:** Implement rotate-on-use refresh tokens

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 3.1 | Add `refreshTokenId` (jti) to `AuthResponse` | `AuthResponse.java`, `AuthService.java` | Low | Response includes refresh token ID |
| 3.2 | Store refresh token `jti` with `used=false` | `AuthService.java:55-70` | Low | DB record created on login |
| 3.3 | On refresh: verify `jti` unused → mark used → issue new pair | `AuthService.java:150-180` | Medium | Old refresh token rejected; new pair issued |
| 3.4 | Add cleanup job for expired/used refresh tokens | New: `TokenCleanupScheduler.java` | Low | Old tokens removed on schedule |

**Exit Criteria:** Refresh flow works; replay attacks blocked; DB doesn't grow unbounded

---

## Phase 4: CORS Restriction (Single config change, high impact)

**Goal:** Fix critical CORS misconfiguration

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 4.1 | Replace `allowedOriginPatterns("*")` with explicit origins per profile | `AppConfig.java:64` | **Medium** | Dev: localhost:3000; Staging/Prod: exact domains |
| 4.2 | Add integration test for CORS preflight rejection | New: `CorsSecurityIT.java` | Low | Unknown origin → 403/empty CORS headers |
| 4.3 | Verify credentials work with restricted origins | `AppConfig.java:63` | Low | Authenticated requests from allowed origin succeed |

**Exit Criteria:** Only configured origins can make credentialed requests

---

## Phase 5: Security Headers (Filter chain only)

**Goal:** Add browser security headers via Spring Security

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 5.1 | Add HSTS, CSP, X-Frame-Options, X-Content-Type-Options, Referrer-Policy | `SecurityConfig.java` | Low | `curl -I` shows all headers |
| 5.2 | Configure CSP report-only mode first | `SecurityConfig.java` | Low | CSP violations reported, not blocked |
| 5.3 | Tighten CSP after verifying no frontend breaks | `SecurityConfig.java` | Medium | Frontend loads; no CSP errors in console |
| 5.4 | Add integration test for headers | `SecurityHeadersIT.java` | Low | All headers present on responses |

**Exit Criteria:** All security headers present; frontend functional

---

## Phase 6: CSRF Protection (Requires careful testing)

**Goal:** Enable CSRF for state-changing endpoints (non-API) or document stateless exception

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 6.1 | **Decision:** Keep CSRF disabled for `/api/**` (stateless JWT) | `SecurityConfig.java:33` | N/A | Documented in ADR |
| 6.2 | Enable CSRF for `/actuator/**`, `/h2-console/**`, Thymeleaf endpoints | `SecurityConfig.java` | Medium | Form submissions work; CSRF token required |
| 6.3 | Add `CsrfTokenRepository` with cookie for non-API routes | `SecurityConfig.java` | Low | CSRF cookie set on login/page load |
| 6.4 | Integration test: CSRF required for state-changing non-API | `CsrfIT.java` | Low | POST without token → 403 |

**Exit Criteria:** CSRF enabled where needed; API unaffected

---

## Phase 7: WebSocket Security (New configuration)

**Goal:** Secure STOMP/WebSocket endpoints

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 7.1 | Implement `WebSocketSecurityConfig` extending `AbstractSecurityWebSocketMessageBrokerConfigurer` | New: `WebSocketSecurityConfig.java` | **Medium** | Config loads; no startup errors |
| 7.2 | Secure `/app/**` (client-to-server) with authentication | `WebSocketSecurityConfig.java` | Medium | Unauthenticated CONNECT → 401 |
| 7.3 | Secure `/topic/**`, `/queue/**` (server-to-client) with authorization | `WebSocketSecurityConfig.java` | Medium | Unauthorized subscribe → 403 |
| 7.4 | Add `@PreAuthorize` to `@MessageMapping` handlers | Chat/Notification controllers | Medium | Unauthorized SEND → 403 |
| 7.5 | Integration test: full WebSocket auth flow | `WebSocketSecurityIT.java` | Low | Authenticated flow works; unauth fails |

**Exit Criteria:** WebSocket requires auth; messages authorized per destination

---

## Phase 8: Method-Level Authorization (Controller changes)

**Goal:** Replace URL-based with annotation-based authorization

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 8.1 | Add `@PreAuthorize("hasRole('ADMIN')")` to admin controllers | `AdminController.java`, etc. | Medium | Non-admin → 403; admin → works |
| 8.2 | Add `@PreAuthorize("hasAuthority('USER_READ')")` to user endpoints | `UserController.java` | Medium | Permission checks enforced |
| 8.3 | Add `@PreAuthorize("@postSecurity.canAccess(#postId, authentication)")` for ownership | `PostController.java`, new `PostSecurityService` | **High** | Owner → access; other → 403 |
| 8.4 | Remove redundant `authorizeHttpRules` for annotated endpoints | `SecurityConfig.java:34-61` | Medium | URL config simplified; tests pass |
| 8.5 | Integration tests for all `@PreAuthorize` rules | `*ControllerSecurityIT.java` | Low | Matrix of role/permission tested |

**Exit Criteria:** Fine-grained authorization enforced; URL config minimal

---

## Phase 9: Token Cleanup & Monitoring (Operational)

**Goal:** Prevent token table bloat; add observability

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 9.1 | Scheduled job: delete expired/used tokens daily | `TokenCleanupScheduler.java` | Low | Token table size stable |
| 9.2 | Metrics: `auth.login.success`, `auth.login.failure`, `auth.token.revoked` | `AuthService.java`, `JwtFilter.java` | Low | Metrics in `/actuator/prometheus` |
| 9.3 | Alert on auth failure rate spike | `application.yml` (Micrometer) | Low | Alert fires on threshold |

**Exit Criteria:** Operational hygiene in place

---

## Phase 10: Documentation & ADRs (Zero code risk)

**Goal:** Record decisions for future maintainers

| Step | Task | Files | Risk | Verification |
|------|------|-------|------|--------------|
| 10.1 | ADR: Stateless JWT + CSRF disabled for API | `docs/adr/001-stateless-jwt-csrf.md` | None | ADR accepted |
| 10.2 | ADR: CORS origin configuration strategy | `docs/adr/002-cors-config.md` | None | ADR accepted |
| 10.3 | ADR: WebSocket authentication approach | `docs/adr/003-websocket-auth.md` | None | ADR accepted |
| 10.4 | Security hardening checklist for releases | `docs/checklists/security-release.md` | None | Checklist used in release |

---

## Dependency Graph & Sequencing Rationale

```
Phase 0 (Foundation)
    │
    ├──→ Phase 1 (Logging) ──────────────────┐
    │                                        │
    ├──→ Phase 2 (JWT Hardening) ────────────┼──→ Phase 3 (Refresh Rotation)
    │                                        │        │
    ├──→ Phase 4 (CORS) ◄────────────────────┘        │
    │                     │                           │
    ├──→ Phase 5 (Headers) ───────────────────────────┤
    │                     │                           │
    ├──→ Phase 6 (CSRF) ◄─────────────────────────────┤
    │                     │                           │
    ├──→ Phase 7 (WebSocket) ◄────────────────────────┤
    │                     │                           │
    └──→ Phase 8 (Method Auth) ◄──────────────────────┘
                              │
                              ├──→ Phase 9 (Cleanup/Monitoring)
                              │
                              └──→ Phase 10 (Documentation)
```

**Key Principles:**
1. **Phases 0-3** touch only auth/JWT internals — no controller/config behavior changes
2. **Phase 4 (CORS)** is first config change with external impact — gated by tests
3. **Phases 5-6** are filter-chain additions — additive, not modifying existing rules
4. **Phase 7 (WebSocket)** is new config — no existing WebSocket security to break
5. **Phase 8 (Method Auth)** is highest risk — runs last when all infrastructure is stable
6. **Phases 9-10** are operational — no functional changes

---

## Rollback Strategy per Phase

| Phase | Rollback Trigger | Rollback Action |
|-------|------------------|-----------------|
| 1-3 | Auth tests fail | Revert single file; JWT changes are backward-compatible |
| 4 | Frontend can't authenticate | Revert `AppConfig.java:64` to `*`; fix origin list |
| 5 | Frontend breaks (CSP) | Disable CSP or use report-only; iterate |
| 6 | Form submissions break | Disable CSRF for affected paths |
| 7 | WebSocket connections fail | Comment out `WebSocketSecurityConfig`; debug |
| 8 | Authorization regressions | Revert specific `@PreAuthorize`; keep URL config |

---

## Test Strategy Alignment

| Phase | New Test Types | Existing Tests to Preserve |
|-------|----------------|----------------------------|
| 0 | `*SecurityIT.java` baseline | All unit/slice/integration |
| 1-3 | JWT validation, revocation, rotation | `JwtFilterTest`, `JwtServiceTest`, `AuthServiceTest` |
| 4 | CORS preflight, credentialed requests | `PostControllerSecurityTest` |
| 5 | Security headers on all responses | Slice tests |
| 6 | CSRF token validation | Form-based controller tests |
| 7 | WebSocket CONNECT/SEND/SUBSCRIBE auth | `ChatFlowIT`, `NotificationFlowIT` |
| 8 | `@PreAuthorize` matrix (role/permission/ownership) | All controller security tests |

---

## Estimated Timeline

| Phase | Effort | Parallelizable? |
|-------|--------|-----------------|
| 0 | 0.5 days | Yes (independent) |
| 1 | 0.5 days | Yes |
| 2 | 1 day | After 1 |
| 3 | 1 day | After 2 |
| 4 | 0.5 days | After 0 |
| 5 | 0.5 days | After 0 |
| 6 | 0.5 days | After 5 |
| 7 | 1.5 days | After 0 |
| 8 | 2 days | After 7 |
| 9 | 0.5 days | After 3 |
| 10 | 0.5 days | Anytime |

**Total: ~9 days sequential** (can overlap Phases 4-7 after Phase 0)

---

## Definition of Done (Per Phase)

- [ ] All new tests pass (green)
- [ ] All existing tests pass (no regressions)
- [ ] Manual verification checklist complete
- [ ] Code reviewed and merged
- [ ] Deployed to staging; smoke tests pass
- [ ] Rollback plan documented and tested

---

## Next Steps

1. Review and approve this plan
2. Create Phase 0 test baseline (I'll start on your command)
3. Begin Phase 1 implementation

**Ready to start when you give the command.**