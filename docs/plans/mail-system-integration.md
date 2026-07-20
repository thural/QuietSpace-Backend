# Mail System Integration — Resend + RabbitMQ Event-Driven Architecture

## Overview

Replace the direct synchronous email dispatch with an event-driven pipeline:
RabbitMQ broker receives lightweight event payloads from web controllers, an async consumer
polls the queue, compiles Thymeleaf templates, and dispatches via Resend REST API over virtual threads.

```
[Web Controller] ──publish──> [RabbitMQ] ──consume──> [Async Consumer]
                                                          │
                                              (virtual thread)
                                                          │
                                                     [Resend API]
```

---

## Phase 1: Dependencies & Configuration

### Step 1.1 — Add RabbitMQ dependency

**File:** `build.gradle.kts`

Add `spring-boot-starter-amqp` to dependencies block.

### Step 1.2 — Add RabbitMQ connection config

**File:** `src/main/resources/application.yml`

Add `spring.rabbitmq` block with env-var defaults for dev pointing to local RabbitMQ.

### Step 1.3 — Add Resend provider config

**File:** `src/main/resources/application.yml`

Add `email.provider` config namespace for provider URL and API key.

### Step 1.4 — Override Resend config for prod

**File:** `src/main/resources/application-prod.yml`

Replace Postmark SMTP block with Resend provider URL/key and RabbitMQ credentials.

---

## Phase 2: Event Infrastructure

### Step 2.1 — Create EmailEvent record

**File:** `dev/thural/quietspace/shared/event/EmailEvent.java` (new)

A lightweight record carrying only `to`, `subject`, `templateName`, and `variables` map.
This is the message payload sent through RabbitMQ.

### Step 2.2 — Create RabbitMQ queue/exchange configuration

**File:** `dev/thural/quietspace/config/RabbitMQConfig.java` (new)

Declare:
- `email.exchange` (topic)
- `email.queue` bound to exchange with routing key `email.send`
- DLX: `email.dlx` → `email.dlq` for failed deliveries

### Step 2.3 — Create EmailEventPublisher

**File:** `dev/thural/quietspace/shared/service/impl/EmailEventPublisher.java` (new)

Wraps `RabbitTemplate` to convert and send `EmailEvent` to the exchange.

### Step 2.4 — Create EmailEventConsumer

**File:** `dev/thural/quietspace/shared/service/impl/EmailEventConsumer.java` (new)

`@RabbitListener` on `email.queue`. Injects `EmailService` (resolved to correct impl by
active profile). Calls `sendHtmlEmail()` synchronously — the listener container thread
handles the work; if it fails, RabbitMQ nacks/retries the message.

### Step 2.5 — Create ResendEmailService

**File:** `dev/thural/quietspace/shared/service/impl/ResendEmailService.java` (new)

Implements `EmailService`, annotated `@Profile("prod")`. Uses `RestClient` to POST
to Resend's REST API (`https://api.resend.com/emails`) with `Authorization: Bearer` header.

### Step 2.6 — Remove PostmarkEmailService

Delete `PostmarkEmailService.java` and its test. No longer needed.

---

## Phase 3: Wire Business Services

### Step 3.1 — Refactor AuthService to publish instead of calling EmailService directly

**File:** `dev/thural/quietspace/auth/AuthService.java`

Replace `emailService.sendHtmlEmail(...)` call with
`emailEventPublisher.publish(new EmailEvent(...))`. The web controller returns 201
immediately after saving the user; the email is handled out-of-band.

---

## Phase 4: Infrastructure

### Step 4.1 — Add RabbitMQ to docker-compose.yaml

**File:** `quietspace-infrastructure/docker-compose.yaml`

Add `rabbitmq:3-management` service with healthcheck, default creds, on `quietspace-network`.

### Step 4.2 — Add RabbitMQ ports to docker-compose.override.yaml

**File:** `quietspace-infrastructure/docker-compose.override.yaml`

Expose RabbitMQ ports `5672:5672` and `15672:15672` for local dev access.

### Step 4.3 — Add RabbitMQ + Resend env vars to .env files

**Files:** `.env.example`, `.env.production.example`, `.env.staging.example`

Add `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD`,
`EMAIL_PROVIDER_URL`, `EMAIL_API_KEY`.

### Step 4.4 — Update cd-infra.yml for Resend secret

**File:** `quietspace-infrastructure/.github/workflows/cd-infra.yml`

Replace `POSTMARK_SERVER_TOKEN` injection with `EMAIL_API_KEY`. Add `RABBITMQ_PASSWORD`.

---

## Phase 5: Testing

### Step 5.1 — Unit test ResendEmailService

Verify correct HTTP call to Resend API with auth header, JSON body, and error propagation.

### Step 5.2 — Unit test EmailEventPublisher

Verify `RabbitTemplate.convertAndSend` called with correct exchange, routing key, payload.

### Step 5.3 — Update AuthServiceTest

Replace `verify(emailService).sendHtmlEmail(...)` expectations with
`verify(emailEventPublisher).publish(...)`.

---

## Execution Order

| Phase | Description | Why this order |
|-------|-------------|----------------|
| 1 | Dependencies & config | Safe — no functional change, enables compilation |
| 2 | Event infrastructure | New classes, no existing code depends on them yet |
| 3 | Wire business services | Flip the switch — AuthService publishes instead of calls directly |
| 4 | Infrastructure | Docker + CI/CD — can be done in parallel or after code works |
| 5 | Tests | Verify everything works |

## Files Changed Summary

| File | Action |
|------|--------|
| `build.gradle.kts` | Edit: add `spring-boot-starter-amqp` |
| `application.yml` | Edit: add `spring.rabbitmq` + `email.provider` |
| `application-prod.yml` | Edit: replace Postmark with Resend + RabbitMQ creds |
| `config/RabbitMQConfig.java` | **New** — exchanges, queues, DLX |
| `shared/event/EmailEvent.java` | **New** — message payload record |
| `shared/service/impl/EmailEventPublisher.java` | **New** — RabbitMQ publisher |
| `shared/service/impl/EmailEventConsumer.java` | **New** — RabbitMQ consumer |
| `shared/service/impl/ResendEmailService.java` | **New** — Resend REST client |
| `shared/service/impl/PostmarkEmailService.java` | **Delete** |
| `auth/AuthService.java` | Edit: swap direct call for event publish |
| `docker-compose.yaml` | Edit: add rabbitmq service |
| `docker-compose.override.yaml` | Edit: expose rabbitmq ports |
| `.env.example` | Edit: add rabbitmq + resend vars |
| `.env.production.example` | Edit: add rabbitmq + resend vars |
| `.env.staging.example` | Edit: add rabbitmq vars |
| `cd-infra.yml` | Edit: replace POSTMARK with EMAIL_API_KEY |
| `PostmarkEmailServiceTest.java` | Delete |
| `ResendEmailServiceTest.java` | **New** |
| `EmailEventPublisherTest.java` | **New** |
| `AuthServiceTest.java` | Edit: update verifications |
