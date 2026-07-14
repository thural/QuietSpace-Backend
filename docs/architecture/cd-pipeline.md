# CD Pipeline

## Overview

The Continuous Deployment (CD) pipeline runs only on the `prod` branch. It builds the Docker image, pushes it to GHCR, and deploys to the production VPS. The CI pipeline (compile, test, package) runs first on `prod` before the CD stages execute.

## Pipeline Stages

```
[ CI: Compile ] ──> [ CI: Test ] ──> [ CI: Package ] ──> [ CD: Build ] ──> [ CD: Deploy ]
                                                         │                   │
                                                         ▼                   ▼
                                                   [ Push to GHCR ]   [ SSH + Docker Compose ]
```

**Note:** The CI stages (compile, test, package) run on both `main` and `prod` branches. The CD stages (build, deploy) only run on `prod`.

## Branch Behavior

| Branch | CI Stages | CD Stages |
|---|---|---|
| `main` | compile, test, package | — |
| `prod` | compile, test, package | build, deploy |

## Stage Details

### 1. Build Docker Image

**Purpose:** Build and push the Docker image to GitHub Container Registry (GHCR).

**Runner:** `ubuntu-22.04`

**Condition:** Only runs on `prod` branch (`if: github.ref == 'refs/heads/prod'`).

**Dependencies:** Requires `package` stage to succeed.

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

### 2. Deploy to VPS

**Purpose:** Deploy the application to the production VPS using Docker Compose.

**Runner:** `ubuntu-22.04`

**Condition:** Only runs on `prod` branch (`if: github.ref == 'refs/heads/prod'`).

**Dependencies:** Requires `build` stage to succeed.

**Steps:**
1. Create deployment folder on VPS via SSH
2. Copy Docker Compose file to VPS via SCP
3. Set environment variables and deploy via SSH

**Deployment Commands:**
```bash
# Create deployment directory
ssh $VPS_USERNAME@$VPS_IP "mkdir -p deployment"

# Copy Docker Compose file
scp docker-compose.yml $VPS_USERNAME@$VPS_IP:deployment/docker-compose-prod.yaml

# Deploy with environment variables
ssh $VPS_USERNAME@$VPS_IP <<EOF
  export DB_USER_PASSWORD=...
  export DB_HOST_NAME=...
  # ... other env vars
  cd deployment
  docker-compose -f docker-compose.yaml pull -q
  docker-compose -f docker-compose.yaml up -d
EOF
```

**Trigger:** Runs after build succeeds.

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                      VPS (Ubuntu)                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │  quietspace-    │  │  quietspace-    │              │
│  │  monolith       │  │  monolith-db    │              │
│  │  (Spring Boot)  │  │  (MySQL 8.0)    │              │
│  │  :8080          │  │  :3306          │              │
│  └────────┬────────┘  └────────┬────────┘              │
│           │                    │                        │
│           └────────────────────┘                        │
│                    │                                    │
│           monolith-network                              │
│                    │                                    │
│  ┌─────────────────┐  ┌─────────────────┐              │
│  │  quietspace-    │  │  mail-dev       │              │
│  │  frontend       │  │  (MailDev)      │              │
│  │  :3000          │  │  :1080, :1025   │              │
│  └─────────────────┘  └─────────────────┘              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

## Container Configuration

### Backend (quietspace-monolith)

| Setting | Value |
|---|---|
| Image | `ghcr.io/<repo>/quietspace:monolith-latest` |
| Port | `8080` |
| Health Check | `/actuator/health` (wget, 10s interval) |
| Depends On | `quietspace-monolith-db` (service_healthy) |
| Restart | `on-failure` |

### Database (quietspace-monolith-db)

| Setting | Value |
|---|---|
| Image | `mysql:8.0` |
| Port | `3306` |
| Health Check | `mysqladmin ping` (5s interval) |
| Volume | `quietspace_monolith_data` (persistent) |
| Restart | `on-failure` |

### Frontend (quietspace-frontend)

| Setting | Value |
|---|---|
| Image | `thural/quietspace:frontend` |
| Port | `80` → `3000` |
| Depends On | `quietspace-monolith` |

### Mail Dev (mail-dev)

| Setting | Value |
|---|---|
| Image | `maildev/maildev` |
| Ports | `1080` (UI), `1025` (SMTP) |
| Depends On | `quietspace-monolith` |

## Required GitHub Secrets

| Secret | Purpose | Example |
|---|---|---|
| `VPS_USERNAME` | SSH username for VPS | `ubuntu` |
| `VPS_IP` | VPS IP address | `192.168.1.100` |
| `DB_ROOT_PASSWORD` | MySQL root password | `rootpassword` |
| `DB_NAME` | Database name | `quietspace` |
| `DB_USER_USERNAME` | Database username | `quietspace_user` |
| `DB_USER_PASSWORD` | Database password | `userpassword` |
| `DB_PORT_NUMBER` | Database port | `3306` |
| `DB_HOST_NAME` | Database hostname | `quietspace-monolith-db` |

## Deployment Commands

### Manual Deployment

```bash
# SSH into VPS
ssh $VPS_USERNAME@$VPS_IP

# Navigate to deployment directory
cd deployment

# Pull latest images
docker-compose -f docker-compose.yaml pull -q

# Start/restart containers
docker-compose -f docker-compose.yaml up -d

# View logs
docker-compose -f docker-compose.yaml logs -f quietspace-monolith

# Check container status
docker-compose -f docker-compose.yaml ps
```

### Local Deployment (Development)

```bash
# Navigate to docker directory
cd infrastructure/docker

# Create network (first time only)
docker network create monolith-network

# Start all containers
docker compose -f docker-compose.yaml up -d

# Start only backend and database
docker compose -f docker-compose.yaml up -d quietspace-monolith-db quietspace-monolith

# View logs
docker compose -f docker-compose.yaml logs -f quietspace-monolith
```

## Health Checks

### Application Health

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### Container Health

```bash
# Check container health status
docker inspect --format='{{.State.Health.Status}}' quietspace-monolith
docker inspect --format='{{.State.Health.Status}}' quietspace-monolith-db
```

### Database Health

```bash
# Check MySQL connectivity
docker exec quietspace-monolith-db mysqladmin ping -u root -p$MYSQL_ROOT_PASSWORD
```

## Rollback Procedures

### Quick Rollback

```bash
# SSH into VPS
ssh $VPS_USERNAME@$VPS_IP

# Stop current containers
docker-compose -f docker-compose.yaml down

# Pull previous version (if tagged)
docker pull ghcr.io/<repo>/quietspace:monolith-<previous-version>

# Update image tag in docker-compose.yaml and restart
docker-compose -f docker-compose.yaml up -d
```

### Full Rollback

```bash
# Stop all containers
docker-compose -f docker-compose.yaml down

# Remove persistent data (if needed)
docker volume rm quietspace_monolith_data

# Restore database from backup
# ...

# Start with previous version
docker-compose -f docker-compose.yaml up -d
```

## Environment Variables

All environment variables are defined in the `.env` file at the project root. See [Configuration Guide](../guide/usage/configuration.md) for details.

| Variable | Purpose | Default |
|---|---|---|
| `DB_HOST_NAME` | MySQL hostname | `quietspace-monolith-db` |
| `DB_PORT_NUMBER` | MySQL port | `3306` |
| `DB_NAME` | Database name | `quietspace` |
| `DB_USER_USERNAME` | Database username | `quietspace_user` |
| `DB_USER_PASSWORD` | Database password | `userpassword` |
| `DB_ROOT_PASSWORD` | MySQL root password | `rootpassword` |
| `SERVER_PORT_NUMBER` | Application port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `MAILDEV_HOST` | Mail server hostname | `mail-dev` |
| `MAILDEV_PORT` | Mail server port | `1025` |
| `FRONTEND_HOST` | Frontend hostname | `localhost` |
| `FRONTEND_PORT` | Frontend port | `3000` |
