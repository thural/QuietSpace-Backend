# ADR-001: Stateless JWT Authentication with CSRF Disabled for API

**Status:** Accepted  
**Date:** 2026-07-21  
**Author:** Security Team

## Context

The QuietSpace backend uses a REST API architecture with JWT-based authentication. Spring Security's default behavior includes CSRF protection, which conflicts with stateless JWT authentication.

## Decision

- **CSRF is disabled** for all `/api/**` endpoints  
- **Session management is stateless** (`SessionCreationPolicy.STATELESS`)  
- Authentication is done exclusively via JWT Bearer tokens

## Rationale

1. **JWT tokens are not browser-automatically attached** — unlike cookies, JWTs must be explicitly sent via `Authorization` header
2. **Stateless design** — no server-side session to protect against CSRF
3. **Bearer tokens are not vulnerable to CSRF** — an attacker cannot force a victim's browser to attach a JWT to a cross-origin request
4. **Browser CORS + same-origin policy** provide additional protection

## Consequences

- **Positive:** Simplified security configuration, no CSRF token management needed
- **Positive:** Stateless scalability
- **Negative:** Should never use cookie-based auth alongside this configuration
- **Mitigation:** If cookie-based auth is needed in the future, CSRF must be re-enabled

## Related

- CORS configuration: ADR-002
- WebSocket auth: ADR-003
