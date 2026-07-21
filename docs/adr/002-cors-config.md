# ADR-002: CORS Origin Configuration Strategy

**Status:** Accepted  
**Date:** 2026-07-21  
**Author:** Security Team

## Context

The QuietSpace backend must accept cross-origin requests from the frontend application. Previously, CORS was configured with `allowedOriginPatterns("*")` which allowed any origin — a security risk when combined with `allowCredentials(true)`.

## Decision

- **Allowed origins are explicitly configured** via `custom.cors.allowed-origins` property
- **`setAllowedOrigins()`** is used instead of `setAllowedOriginPatterns("*")`
- No wildcard origins are permitted with credentialed requests
- Each deployment environment (dev, staging, prod) defines its own allowed origins

## Configuration

```yaml
# application.yml (default)
custom:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:5173

# application-prod.yml
custom:
  cors:
    allowed-origins: https://quietspace.app,https://admin.quietspace.app
```

## Rationale

1. **`Access-Control-Allow-Origin: *` with `Access-Control-Allow-Credentials: true` is a security violation** per the CORS specification — browsers reject this combination
2. **Explicit origins** prevent unauthorized cross-origin data access
3. **Environment-specific configuration** allows flexible development while maintaining production security

## Consequences

- **Positive:** Only trusted origins can make credentialed requests
- **Positive:** Prevents CSRF-like attacks from untrusted origins
- **Negative:** New frontend domains must be explicitly added before they can make API calls
- **Mitigation:** CI/CD pipeline validates CORS config during deployment

## Related

- Security config: ADR-001
- WebSocket CORS handled separately in WebSocketConfig
