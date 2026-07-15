# CI/CD Architecture

This repository is a **stateless builder** — it only runs tests, builds a Docker image, and pushes it to the GitHub Container Registry (GHCR).

All deployment concerns (VPS access, Docker Compose, environment secrets) are handled by a dedicated **infrastructure repository**, which listens for repository dispatch events from this pipeline. That repository is the single source of truth for production configuration and holds all VPS-level secrets.
