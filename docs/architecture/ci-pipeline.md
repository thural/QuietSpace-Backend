# CI Pipeline

## Overview

The Continuous Integration (CI) pipeline runs automatically on every push to `main` or `prod` branches when source code, build configuration, or Docker files change.

## Pipeline Stages

```
[ Compile ] ──> [ Test ] ──> [ Package ] ──> [ Build Docker Image ]
     │              │              │                    │
     └──────────────┴──────────────┘                    │
         (runs in parallel)                             ▼
                                              [ Push to GHCR ]
```

## Stage Details

### 1. Compile

**Purpose:** Verify that the Java source code compiles without errors.

**Runner:** `ubuntu-22.04`

**Steps:**
1. Checkout code with full git history (`fetch-depth: 0`)
2. Setup JDK 25 (Amazon Corretto)
3. Setup Gradle with caching (`gradle/actions/setup-gradle@v4`)
4. Run `./gradlew clean compileJava`

**Trigger:** Always runs on pipeline trigger.

### 2. Test

**Purpose:** Execute unit tests to verify code correctness.

**Runner:** `ubuntu-22.04`

**Steps:**
1. Checkout code with full git history
2. Setup JDK 25 (Amazon Corretto)
3. Setup Gradle with caching
4. Run `./gradlew clean test`

**Test Configuration:**
- Unit tests: All tests except `*IT`, `*ITCase`, `*FlowIT` patterns
- Integration tests: Separate task (`integrationTest`) for `*IT`, `*FlowIT`, `*ITCase`
- Test database: H2 in-memory (MySQL compatibility mode)
- Testcontainers: Enabled for MySQL integration tests

**Trigger:** Always runs on pipeline trigger (parallel with Compile).

### 3. Package

**Purpose:** Build the executable Spring Boot JAR (bootJar).

**Runner:** `ubuntu-22.04`

**Dependencies:** Requires `compile` and `test` to succeed.

**Steps:**
1. Checkout code with full git history
2. Setup JDK 25 (Amazon Corretto)
3. Setup Gradle with caching
4. Run `./gradlew clean bootJar`

**Output:** `build/libs/quietspace-0.0.1-SNAPSHOT.jar` (layered JAR)

**Trigger:** Runs after compile and test succeed.

### 4. Build Docker Image

**Purpose:** Build and push the Docker image to GitHub Container Registry (GHCR).

**Runner:** `ubuntu-22.04`

**Dependencies:** Requires `package` to succeed.

**Steps:**
1. Checkout code with full git history
2. Setup JDK 25 (Amazon Corretto)
3. Setup Gradle with caching
4. Extract project version from `build.gradle.kts`
5. Login to GHCR using `GITHUB_TOKEN`
6. Build and push Docker image

**Docker Build:**
- **Base image:** `eclipse-temurin:25-jre-alpine`
- **Build context:** Project root (`./`)
- **Dockerfile:** `infrastructure/docker/Dockerfile`
- **Platform:** `linux/amd64`
- **Tags:**
  - `ghcr.io/<repo>/quietspace:monolith-<version>`
  - `ghcr.io/<repo>/quietspace:monolith-latest`

**Trigger:** Runs after package succeeds.

## Trigger Configuration

The pipeline triggers on pushes to `main` or `prod` branches when any of these files change:

```yaml
paths:
  - 'src/**'                                    # Source code changes
  - 'build.gradle.kts'                          # Build configuration changes
  - 'infrastructure/docker/docker-compose.yaml' # Docker Compose changes
  - 'infrastructure/docker/Dockerfile'          # Dockerfile changes
  - '.github/workflows/*-monolith.yml'          # Pipeline changes
```

## Caching Strategy

| Cache | Mechanism | Benefit |
|---|---|---|
| Gradle dependencies | `gradle/actions/setup-gradle@v4` | Faster subsequent builds |
| Docker layers | Docker BuildKit (multi-stage) | Reuse unchanged layers |
| GitHub Actions | Built-in action cache | Faster step execution |

## Required GitHub Secrets

| Secret | Purpose | Used In |
|---|---|---|
| `GITHUB_TOKEN` | GHCR authentication | Build stage (auto-provided) |

## Artifacts

| Artifact | Location | Retention |
|---|---|---|
| Spring Boot JAR | `build/libs/quietspace-*.jar` | Build only (not persisted) |
| Docker Image | `ghcr.io/<repo>/quietspace` | Permanent |

## Running CI Locally

To run the CI pipeline locally:

```bash
# Compile
./gradlew clean compileJava

# Test
./gradlew clean test

# Package
./gradlew clean bootJar

# Build Docker image
docker compose -f infrastructure/docker/docker-compose.yaml build quietspace-monolith
```
