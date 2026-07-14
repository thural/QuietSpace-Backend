# CI/CD Architecture Overview

## System Architecture

```
 Developer Push ──> GitHub Repository
                          │
                          ▼
                  [ GitHub Actions ]
                          │
         ┌────────────────┴────────────────┐
         ▼                                 ▼
   [ CI Pipeline ]                   [ CD Pipeline ]
   - Compile                        - Build Docker Image
   - Test                           - Push to GHCR
   - Package                        - Deploy to VPS
         │                                 │
         └────────────────┬────────────────┘
                          ▼
                    [ Cloud VPS ]
                  (Ubuntu + Docker)
                          │
                          ▼
              [ Running Application ]
              - Spring Boot (REST + WebSocket)
              - MySQL 8.0
              - MailDev (dev only)
              - Frontend
```

## Pipeline Overview

| Stage | Tool | Purpose |
|---|---|---|
| Source Control | GitHub | Code hosting, branch protection, PR reviews |
| CI Engine | GitHub Actions | Automated build, test, package |
| Container Registry | GHCR (ghcr.io) | Docker image storage |
| Deployment Target | VPS (Ubuntu) | Running containers via Docker Compose |

## Branch Strategy

| Branch | Purpose | Pipeline Trigger |
|---|---|---|
| `main` | Development branch | CI + CD on push |
| `prod` | Production branch | CI + CD on push |
| `feature/*` | Feature development | CI only (on PR) |

## Key Files

| File | Purpose |
|---|---|
| `.github/workflows/pipeline-monolith.yml` | GitHub Actions CI/CD pipeline |
| `infrastructure/docker/Dockerfile` | Multi-stage Docker build |
| `infrastructure/docker/docker-compose.yaml` | Container orchestration |
| `.env` | Environment variables (single source of truth) |
| `.dockerignore` | Docker build context exclusions |

## Related Documentation

- [CI Pipeline Details](ci-pipeline.md)
- [CD Pipeline Details](cd-pipeline.md)
- [Configuration Guide](../guide/usage/configuration.md)
