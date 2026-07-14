# CI/CD Pipeline Hardening Plan

## Overview

This plan addresses gaps in the QuietSpace-Backend CI/CD pipeline by fixing critical issues, aligning with the industry-standard blueprint (GitHub Actions + Docker + GHCR), and adding production-readiness features.

---

## Phase 1: Fix Critical Issues

### Step 1.1: Create `.dockerignore`

Create a `.dockerignore` file at the project root to prevent secrets, build artifacts, and unnecessary files from leaking into the Docker build context.

**Files:** `.dockerignore` (new)

### Step 1.2: Add Spring Boot Actuator

Add the `spring-boot-starter-actuator` dependency to `build.gradle.kts` and configure health endpoints in `application.yml`. This provides `/actuator/health` for Docker and Kubernetes orchestration.

**Files:** `build.gradle.kts`, `src/main/resources/application.yml`, `src/main/resources/application-prod.yml`

### Step 1.3: Remove K8s Secrets from Repository

Delete `infrastructure/k8s/common-secret.yaml` (contains base64-encoded passwords committed to repo). Add it to `.gitignore`. Users must create secrets manually or use a secrets manager.

**Files:** `infrastructure/k8s/common-secret.yaml` (delete), `.gitignore`

### Step 1.4: Fix Workflow Trigger Paths

Update `.github/workflows/pipeline-monolith.yml` trigger paths to match actual file locations (`infrastructure/docker/Dockerfile`, `infrastructure/docker/docker-compose.yaml`).

**Files:** `.github/workflows/pipeline-monolith.yml`

### Step 1.5: Fix Version Extraction

The workflow calls `./gradlew -q printVersion` but no such task exists. Replace with reading version from `build.gradle.kts` using grep/sed.

**Files:** `.github/workflows/pipeline-monolith.yml`

### Step 1.6: Fix Deploy Step Syntax Errors

Fix `export` statements in the deploy job that use `:` instead of `=` (lines 143-146).

**Files:** `.github/workflows/pipeline-monolith.yml`

### Step 1.7: Fix Docker Compose `.env` Path

Update `docker-compose.yaml` to reference the `.env` file correctly, or copy `.env` to `infrastructure/docker/` during deployment. Use `env_file` with correct relative path.

**Files:** `infrastructure/docker/docker-compose.yaml`

---

## Phase 2: Align with Blueprint

### Step 2.1: Add App Health Check to Docker Compose

Add a health check to the `quietspace-monolith` service using the Actuator `/actuator/health` endpoint. Update `depends_on` to use `condition: service_healthy`.

**Files:** `infrastructure/docker/docker-compose.yaml`

### Step 2.2: Add K8s Readiness/Liveness Probes

Add readiness and liveness probes to the backend Kubernetes deployment using the Actuator health endpoint.

**Files:** `infrastructure/k8s/deployments/services/backend.yaml`

### Step 2.3: Switch from DockerHub to GHCR

Update the GitHub Actions workflow to push Docker images to GitHub Container Registry (ghcr.io) instead of DockerHub. GHCR is free for public and private repositories.

**Files:** `.github/workflows/pipeline-monolith.yml`

### Step 2.4: Fix K8s PV Storage Path

Replace the placeholder path `/path/to/persistent/storage` in the PersistentVolume manifest with a real hostPath or use a proper storage class.

**Files:** `infrastructure/k8s/deployments/databases/pv.yaml`

---

## Phase 3: Verification

### Step 3.1: Validate Docker Build

Run `docker compose build` to verify the Dockerfile and `.dockerignore` work correctly.

### Step 3.2: Validate Workflow Syntax

Use `actionlint` or review the GitHub Actions workflow for syntax correctness.

### Step 3.3: Validate K8s Manifests

Run `kubectl apply --dry-run=client` on K8s manifests to validate syntax.

---

## Summary of Changes

| Phase | Files Modified | Files Created | Files Deleted |
|---|---|---|---|
| Phase 1 | `build.gradle.kts`, `application.yml`, `application-prod.yml`, `.github/workflows/pipeline-monolith.yml`, `infrastructure/docker/docker-compose.yaml`, `.gitignore` | `.dockerignore` | `infrastructure/k8s/common-secret.yaml` |
| Phase 2 | `infrastructure/docker/docker-compose.yaml`, `infrastructure/k8s/deployments/services/backend.yaml`, `.github/workflows/pipeline-monolith.yml`, `infrastructure/k8s/deployments/databases/pv.yaml` | — | — |
| Phase 3 | — | — | — |
