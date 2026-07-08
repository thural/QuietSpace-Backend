# Session Changes

## Overview
Changes made during this session to enable live documentation for REST and WebSocket endpoints.

---

## Files Modified

### `pom.xml`
- Removed explicit version override `3.1.2` from `spring-boot-starter-data-jpa` to prevent Spring Boot 4 transitive dependency conflicts.
- Added **Springwolf** dependencies for AsyncAPI WebSocket documentation:
  - `io.github.springwolf:springwolf-stomp:1.21.0`
  - `io.github.springwolf:springwolf-ui:1.21.0`

### `src/main/resources/application.yml`
- Added Springwolf docket configuration:
  - `springwolf.docket.base-package: dev.thural.quietspace`
  - `springwolf.docket.info.title: QuietSpace API`
  - `springwolf.docket.info.version: 1.0`
  - `springwolf.docket.servers.stomp.protocol: stomp`
  - `springwolf.docket.servers.stomp.host: localhost:8080`
- Added `spring.main.allow-bean-definition-overriding: true` to resolve bean definition override errors during startup.

### `src/main/java/dev/thural/quietspace/security/SecurityConfig.java`
- Fixed missing leading slashes on security whitelist paths:
  - `api/v1/admin/**` → `/api/v1/admin/**`
  - `api/v1/ws/**` → `/api/v1/ws/**`
  - `api/v1/auth/**` → `/api/v1/auth/**`
- Added `/springwolf/**` to the `.permitAll()` whitelist so documentation endpoints are publicly accessible without authentication.

---

## Files Created

### `RUNNING.md`
Quick-start guide covering:
- Docker MySQL container setup
- Required environment variables
- Maven run commands
- Live documentation URLs (`/swagger-ui/index.html`, `/v3/api-docs`, `/springwolf/asyncapi-ui.html`, `/springwolf/docs`)
