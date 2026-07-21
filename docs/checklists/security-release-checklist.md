# Security Hardening Checklist

Use this checklist before each production release to verify security posture.

## Authentication & Authorization

- [ ] JWT secret key is rotated and not a default/known value
- [ ] JWT expiration times are validated (access: ≤15min, refresh: ≤24h)
- [ ] Refresh token rotation is working (old tokens rejected after use)
- [ ] Token blacklist is active (logged-out tokens rejected)
- [ ] `@PreAuthorize` / `@Secured` annotations are present on all admin endpoints
- [ ] No hardcoded credentials in configuration

## Web Security

- [ ] CORS configuration lists only known frontend origins
- [ ] CSRF is either properly enabled or documented as disabled (stateless JWT API)
- [ ] Security headers present:
  - [ ] `Strict-Transport-Security` (HSTS)
  - [ ] `X-Frame-Options: DENY`
  - [ ] `X-Content-Type-Options: nosniff`
  - [ ] `Referrer-Policy: strict-origin-when-cross-origin`
  - [ ] `Content-Security-Policy` (at least report-only)

## WebSocket Security

- [ ] WebSocket connections require JWT authentication
- [ ] Destination-based authorization is configured
- [ ] Unauthenticated CONNECT attempts are rejected
- [ ] Unauthorized SUBSCRIBE attempts are rejected

## Data Protection

- [ ] Passwords hashed with BCrypt (cost ≥ 10)
- [ ] No plaintext secrets in logs
- [ ] No PII in logs unless masked
- [ ] Database connections use TLS in production
- [ ] JPA `open-in-view` is disabled (already: `spring.jpa.open-in-view: false`)

## Secrets Management

- [ ] All secrets use environment variables (not hardcoded)
- [ ] `.env` file is in `.gitignore`
- [ ] CI/CD pipeline does not log secrets
- [ ] Production secrets are in a vault or secrets manager
- [ ] Secret rotation schedule is documented

## Monitoring

- [ ] Security audit logging is enabled (`audit` actuator endpoint)
- [ ] Failed authentication attempts are logged
- [ ] Access denied events are logged
- [ ] Dependency vulnerability scanning ran and passed
- [ ] No HIGH/CRITICAL CVEs in production dependencies

## Testing

- [ ] All unit tests pass
- [ ] Security integration tests pass
- [ ] CORS preflight tests pass
- [ ] WebSocket auth tests pass
- [ ] No test credentials in production configuration

## Deployment

- [ ] Flyway migrations run successfully
- [ ] `ddl-auto: validate` (not `update` or `create-drop`)
- [ ] Actuator endpoints restricted (`/actuator/health` only public)
- [ ] Swagger/OpenAPI docs restricted or disabled in production
- [ ] Server header not leaking version info
