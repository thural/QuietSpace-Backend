# CI Pipeline

## Overview

The Continuous Integration (CI) pipeline runs automatically on every push to `main` or `prod` branches when source code, build configuration, or Docker files change. The CI pipeline handles verification only — it answers the question: "Is this code safe to merge and free of regressions?"

## Pipeline Stages

```
[ Compile ] ──> [ Test ] ──> [ Package ]
     │              │              │
     └──────────────┴──────────────┘
         (runs in parallel)
```

**Note:** The Build Docker Image and Deploy stages are part of the [CD Pipeline](cd-pipeline.md) and only run on the `prod` branch.

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

## Trigger Configuration

The CI pipeline triggers on pushes to `main` or `prod` branches when any of these files change:

```yaml
paths:
  - 'src/**'                         # Source code changes
  - 'build.gradle.kts'               # Build configuration changes
  - 'infrastructure/docker/**'       # Docker files changes
  - '.github/workflows/*-monolith.yml' # Pipeline changes
```

**Branch behavior:**
| Branch | CI Stages | CD Stages |
|---|---|---|
| `main` | compile, test, package | — |
| `prod` | compile, test, package | build, deploy |

## Caching Strategy

| Cache | Mechanism | Benefit |
|---|---|---|
| Gradle dependencies | `gradle/actions/setup-gradle@v4` | Faster subsequent builds |
| GitHub Actions | Built-in action cache | Faster step execution |

## Required GitHub Secrets

| Secret | Purpose | Used In |
|---|---|---|
| `GITHUB_TOKEN` | GHCR authentication | Build stage (auto-provided) |

## Artifacts

| Artifact | Location | Retention |
|---|---|---|
| Spring Boot JAR | `build/libs/quietspace-*.jar` | Build only (not persisted) |

## Running CI Locally

To run the CI pipeline locally:

```bash
# Compile
./gradlew clean compileJava

# Test
./gradlew clean test

# Package
./gradlew clean bootJar
```
