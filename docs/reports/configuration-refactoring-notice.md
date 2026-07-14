# Notice: Configuration Refactoring and Best Practices Implementation

**Date:** July 11, 2026  
**Status:** Completed and Merged

---

## Overview

This notice documents critical refactoring and security/performance adjustments made to the Spring Boot application configuration files under `src/main/resources`. These changes align the project configuration with standard Spring Boot best practices, enforce DRY (Don't Repeat Yourself) principles, improve modularity, and clean up obsolete resources.

---

## Key Changes

### 1. Enforcing Database Connection Best Practices
- **Change:** Disabled **Open Session in View (OSIV)** by default (`spring.jpa.open-in-view: false`) in the base configuration file [application.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application.yml).
- **Rationale:** OSIV keeps the database connection open during the entire web request execution (including JSON serialization). In production environments, this can exhaust the database connection pool under load and hide performance issues (such as N+1 queries executed outside of active transaction blocks).
- **Action Required for Developers:** When developing new APIs, ensure that any lazy-loaded relations required by the controller are explicitly fetched using JPA EntityGraphs (`@EntityGraph`) or repository `JOIN FETCH` queries.

### 2. Parameterizing Mail Credentials
- **Change:** Removed hardcoded credentials (`thural` / `thural`) from the default [application.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application.yml) file and replaced them with parameterizable property placeholders:
  ```yaml
  username: ${MAIL_USERNAME:thural}
  password: ${MAIL_PASSWORD:thural}
  ```
- **Rationale:** Ensures that default local credentials (such as those for Maildev) remain convenient for developer setup while preventing credentials from being hardcoded or committed into version control for non-development environments.

### 3. DRY Configuration and Profile Simplification
- **Change:** Moved global configuration properties (e.g., JDBC driver class name, database type, and default Flyway migration folders) from profile-specific files ([application-dev.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application-dev.yml), [application-prod.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application-prod.yml)) into the default [application.yml](file:///home/thural/Github/QuietSpace-Backend/src/main/resources/application.yml).
- **Change:** Deleted `application-k8s.yml`.
- **Rationale:** 
  - Consolidation prevents property divergence and copy-paste errors across profiles.
  - The Kubernetes-specific profile was 95% identical to `application-prod.yml`. Following the Twelve-Factor App methodology, environmental configuration (such as ports and credentials) should be injected via container environment variables. In Kubernetes manifests, map secret/config values directly to the standard environment variable names used by `application-prod.yml` (e.g., `DB_USER_USERNAME`, `DB_USER_PASSWORD`, `DB_URL`, `SERVER_PORT_NUMBER`) to avoid maintaining a separate configuration profile.

### 4. Codebase Cleanliness and Dead Configuration Removal
- **Change:** Deleted `src/main/resources/META-INF/services/org.keycloak.events.EventListenerProviderFactory`.
- **Rationale:** This file registered `dev.thural.quietspace.keycloak.KeycloakEventListenerFactory` for SPI loading, but no such class exists in the Java codebase. Removing it prevents class-path loader errors/warnings during builds and boot diagnostics.

---

## Current Configuration Overview

```
src/main/resources/
├── application.yml        # Base global configurations (OSIV disabled, virtual threads, Flyway defaults)
├── application-dev.yml    # Development overrides (ddl-auto: update, dev logging levels, server.port)
└── application-prod.yml   # Production overrides (ddl-auto: validate, Hikari pool sizing, flyway enabled)
```
