# Mail Service Integration Plan — QuietSpace

## Overview

Implement a Spring Boot mail system with environment-isolated switching between Mailpit (development) and Postmark (production) via Spring profiles, integrated into the existing GitOps CI/CD pipeline without restructuring the infrastructure or workflow.

---

## Phase 1: Backend Dependencies & Configuration

### Step 1.1 — Add Thymeleaf dependency

**File:** `quietspace-backend/build.gradle.kts`

Add `spring-boot-starter-thymeleaf` to the dependencies block. This replaces the current manual `String.replace()` template processing with Thymeleaf's robust template engine, enabling cleaner HTML email templates with proper variable injection.

### Step 1.2 — Add Postmark SMTP config to application-prod.yml

**File:** `quietspace-backend/src/main/resources/application-prod.yml`

Add `spring.mail` override block with Postmark SMTP settings:

- `host: smtp.postmarkapp.com`
- `port: 587`
- `username: {{POSTMARK_SERVER_TOKEN}}` (Postmark uses the server token as the SMTP username)
- `password: {{POSTMARK_SERVER_TOKEN}}` (Postmark uses the server token as the SMTP password)
- `properties.mail.smtp.auth: true`
- `properties.mail.smtp.starttls.enable: true`

These override the base `application.yml` mail settings when `prod` profile is active. The base config continues to point to `mail-dev:1025` for the `dev` profile.

### Step 1.3 — Add POSTMARK_SERVER_TOKEN placeholder to base application.yml

**File:** `quietspace-backend/src/main/resources/application.yml`

No change needed — the Postmark credentials are set exclusively in `application-prod.yml`. The base config's `spring.mail.username` and `spring.mail.password` use default values (`thural`) that work for local Mailpit (which doesn't enforce auth). The Postmark token is referenced directly in the prod config.

---

## Phase 2: Email Service Abstraction

### Step 2.1 — Create EmailService interface

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/shared/service/EmailService.java` (new)

Extract an `EmailService` interface from the existing concrete class:

```java
public interface EmailService {
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
```

This achieves Dependency Inversion — controllers and services depend on the abstraction, not a specific implementation.

### Step 2.2 — Refactor existing EmailService into SmtpEmailService

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/shared/service/impl/SmtpEmailService.java` (renamed/refactored from `EmailService.java`)

- Rename `EmailService` → `SmtpEmailService`
- Implement `EmailService` interface
- Annotate with `@Profile("dev")` and `@Service`
- Replace `ResourceLoader` + `String.replace()` with Thymeleaf's `SpringTemplateEngine`
- Annotate `sendHtmlEmail` with `@Async("emailExecutor")`
- Keep `@RequiredArgsConstructor` with `JavaMailSender` injection
- The `sendHtmlEmail` method signature uses the interface contract

### Step 2.3 — Create PostmarkEmailService for production

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/shared/service/impl/PostmarkEmailService.java` (new)

- Implement `EmailService` interface
- Annotate with `@Profile("prod")` and `@Service`
- Use `RestTemplate` to call Postmark's REST API (`POST https://api.postmarkapp.com/email`)
- Inject `POSTMARK_SERVER_TOKEN` from config
- Annotate `sendHtmlEmail` with `@Async("emailExecutor")`
- Set `X-Postmark-Server-Token` header and JSON body with `From`, `To`, `Subject`, `HtmlBody`

### Step 2.4 — Create AsyncConfig for dedicated email executor

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/config/AsyncConfig.java` (new)

- Create a `@Configuration` class implementing `AsyncConfigurer`
- Override `getAsyncExecutor()` to return a `TaskExecutorAdapter` backed by `Executors.newVirtualThreadPerTaskFactory()`
- Bean name: `emailExecutor`
- This isolates email network I/O from the main request threads, preventing slow SMTP/API calls from degrading web performance

### Step 2.5 — Create a dedicated email properties record

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/config/EmailProperties.java` (new)

- A `@ConfigurationProperties(prefix = "spring.mail")` record/class
- Provides type-safe access to `host`, `port`, `username`, `password`
- Injected into `PostmarkEmailService` (Postmark uses username=token for SMTP auth) and `SmtpEmailService`

---

## Phase 3: Email Templates

### Step 3.1 — Convert activate_account.html to Thymeleaf

**File:** `quietspace-backend/src/main/resources/templates/activate_account.html`

Replace `{{username}}`, `{{confirmationUrl}}`, `{{activation_code}}` placeholders with Thymeleaf attributes:
- `th:text="${username}"`
- `th:href="${confirmationUrl}"`
- `th:text="${activationCode}"`

Use proper XML-valid HTML5 structure with `xmlns:th="http://www.thymeleaf.org"`.

### Step 3.2 — Create a plain text email template (optional, for fallback)

**File:** `quietspace-backend/src/main/resources/templates/activate_account.txt` (new)

A Thymeleaf plain text version for email clients that don't render HTML. Uses `th:text` for variables.

---

## Phase 4: Infrastructure — Docker Compose & Nginx

### Step 4.1 — Swap maildev to Mailpit in docker-compose.yaml

**File:** `quietspace-infrastructure/docker-compose.yaml`

Replace the `mail-dev` service with `mailpit` using `axllent/mailpit` image and rename the container to `quietspace-mailpit`.

### Step 4.2 — Update Mailpit web UI port in docker-compose.override.yaml

**File:** `quietspace-infrastructure/docker-compose.override.yaml`

Change the mailpit web UI port mapping from `"1080:1080"` to `"8025:8025"` (Mailpit's default web UI port).

SMTP port `"1025:1025"` remains unchanged.

### Step 4.3 — Update Nginx upstream port for mail UI

**File:** `quietspace-infrastructure/nginx.conf`

Rename `maildev_upstream` to `mailpit_upstream` and change server from `mail-dev:8025` to `mailpit:8025`.

The `/mail/` location block stays unchanged — only the upstream name and port change.

### Step 4.4 — Add POSTMARK_SERVER_TOKEN to .env.example

**File:** `quietspace-infrastructure/.env.example`

Add a commented entry documenting the new optional-but-required-for-prod token:

```
# POSTMARK_SERVER_TOKEN=  (required for production email delivery)
```

---

## Phase 5: CI/CD Pipeline

### Step 5.1 — Inject POSTMARK_SERVER_TOKEN in CD workflow

**File:** `quietspace-infrastructure/.github/workflows/cd-infra.yml`

Add one line to the `.env` creation step:

```bash
echo "POSTMARK_SERVER_TOKEN=${{ secrets.POSTMARK_SERVER_TOKEN }}" >> .env
```

### Step 5.2 — Add POSTMARK_SERVER_TOKEN as GitHub Secret

**Action:** Manual — add `POSTMARK_SERVER_TOKEN` as a repository secret in `quietspace-infrastructure` on GitHub.

No workflow file changes beyond Step 5.1.

---

## Phase 6: Update AuthService to use new EmailService interface

### Step 6.1 — Refactor AuthService to depend on EmailService interface

**File:** `quietspace-backend/src/main/java/dev/thural/quietspace/auth/AuthService.java`

- Change injected type from the concrete `EmailService` class to the `EmailService` interface
- Update method call from `sendEmail(...)` to `sendHtmlEmail(to, subject, templateName, variables)`

---

## Summary: Execution Order

| Phase | Description | Files Touched |
|-------|-------------|---------------|
| 1 | Backend dependencies & config | `build.gradle.kts`, `application-prod.yml` |
| 2 | Email service abstraction | `EmailService.java` (new), `SmtpEmailService.java` (refactor), `PostmarkEmailService.java` (new), `AsyncConfig.java` (new), `EmailProperties.java` (new) |
| 3 | Email templates | `activate_account.html` (convert to Thymeleaf) |
| 4 | Infrastructure | `docker-compose.yaml`, `docker-compose.override.yaml`, `nginx.conf`, `.env.example` |
| 5 | CI/CD pipeline | `cd-infra.yml` |
| 6 | AuthService integration | `AuthService.java` |

No changes to: `quietspace-frontend` (any file), CI workflow files in backend/frontend repos, the GitOps repo directory structure, or the repository dispatch mechanism.
