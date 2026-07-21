# ADR-003: WebSocket Authentication Approach

**Status:** Accepted  
**Date:** 2026-07-21  
**Author:** Security Team

## Context

The QuietSpace backend uses STOMP over WebSocket for real-time messaging (chat, notifications). WebSocket connections must be authenticated and authorized to prevent unauthorized access.

## Decision

- **JWT Bearer token** is sent as a native header during STOMP CONNECT frame
- **Token validation** happens in a `ChannelInterceptor` on the inbound channel
- **Destination-based authorization** via `MessageMatcherDelegatingAuthorizationManager`:
  - `/public/**` — permit all (unauthenticated)
  - `/app/**` — authenticated (client-to-server messages)
  - `/user/**`, `/private/**` — authenticated (server-to-client messages)
  - Any other destination — denied
- **Security context propagation** is handled via `TaskDecorator` for async message processing

## Flow

1. Client connects to `/ws` endpoint with `Authorization: Bearer <token>` header
2. `CustomHandshakeHandler` validates token during HTTP handshake
3. `ChannelInterceptor` validates and sets `Principal` on CONNECT frame
4. `MessageMatcherDelegatingAuthorizationManager` authorizes each subsequent message by destination
5. `TaskDecorator` propagates `SecurityContext` to async message handlers

## Rationale

1. **Bearer tokens are the existing auth mechanism** — reusing JWT avoids a separate auth system
2. **Channel-level validation** ensures every message is authenticated
3. **Destination-based authorization** provides fine-grained access control
4. **Security context propagation** prevents authentication loss in async execution

## Consequences

- **Positive:** Consistent auth mechanism across HTTP and WebSocket
- **Positive:** Destination-level access control prevents unauthorized subscriptions
- **Negative:** WebSocket handshake cannot use cookies for auth (must send token manually)
- **Negative:** STOMP clients must handle token refresh and reconnection

## Related

- JWT implementation: JwtService.java
- WebSocket config: WebSocketConfig.java
- Security config: WebSocketSecurityConfig.java
