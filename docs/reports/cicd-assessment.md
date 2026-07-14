# CI/CD Assessment — QuietSpace Backend

> Analysis of the existing continuous integration / continuous deployment configuration for the
> QuietSpace Spring Boot backend. Findings are based on the repository state at the time of review.
>
> **Status: All issues in this report have been resolved.** See
> [CI Pipeline](../architecture/ci-pipeline.md) and [CD Pipeline](../architecture/cd-pipeline.md)
> for the current pipeline configuration.

## Verdict

A CI/CD pipeline **exists**, but it is **immature and currently broken** — it would fail before
completion as written. It is **not production-ready**.

Maturity score: **~3 / 10**.

---

## What exists

A single GitHub Actions workflow:

- `.github/workflows/pipeline-monolith.yml`

It defines a linear job chain triggered on push to `main` / `prod`:

```
compile → test → package → build → deploy
```

- `compile` — `./gradlew clean compileJava`
- `test` — `./gradlew clean test`
- `package` — `./gradlew clean bootJar`
- `build` — builds & pushes a Docker image to DockerHub
- `deploy` — SSH into a VPS, copy a compose file, export secrets, and `docker-compose up -d`

---

## Critical bugs (pipeline will fail)

1. **`printVersion` task is not defined** (`pipeline-monolith.yml:104`)
   The `build` job runs `./gradlew -q printVersion`, but `build.gradle.kts` has no such task (the
   version is only set as `version = "0.0.1-SNAPSHOT"`, line 9). The `build` job errors out, so the
   `deploy` job never runs.

2. **Broken `paths` filters** — they reference files that do not exist:
   - `docker-compose-prod.yaml` (line 9) — actual file is `infrastructure/docker/docker-compose.yaml`.
   - `./docker/prod/Dockerfile` (line 10) — actual file is `infrastructure/docker/Dockerfile`.
   - `./github/workflows/*-monolith.yml` (line 11) — should be `.github/...`; the leading `.` breaks
     the match.
   These silently prevent triggers or point at the wrong artifacts.

3. **Deploy step is internally inconsistent** (lines 132–149)
   It `scp`s `docker-compose.yml` (a root file that does not exist; the actual file is
   `docker-compose.yaml`), then runs `docker-compose -f docker-compose.yaml ...`. It also uses the
   legacy `docker-compose` v1 CLI.

4. **Shell syntax error in deploy heredoc** (line 143)
   `export MYSQL_ROOT_PASSWORD: ${{secrets.DB_ROOT_PASSWORD}}` uses `:` instead of `=` — invalid bash
   and will abort the deploy script.

5. **Dockerfile ignores the supplied build args**
   `build-args: PROFILE, VERSION` (lines 120–122) are not declared in `infrastructure/docker/Dockerfile`,
   so they are silently dropped.

---

## Best-practice gaps

- **No CI on pull requests** — tests run only after merging to `main`/`prod`, so broken code reaches
  the main branch before it is caught.
- **No `concurrency` group** — overlapping pushes can race on the Docker tag push and the VPS deploy.
- **No Gradle/dependency caching reuse across jobs** — each of the four jobs re-checks out and
  re-sets-up JDK/Gradle from scratch; no `actions/upload-artifact` / `download-artifact` passes the
  built jar between jobs, so `package` and `build` recompile redundantly.
- **No test report publishing** — `build/reports/tests` is never uploaded as an artifact.
- **`fetch-depth: 0` on every job** — unnecessary and slows checkout.
- **No `permissions:` least-privilege**, no `workflow_dispatch`, no deploy verification / health-check /
  rollback, no image signing.
- **Deploy secrets exported in a remote `ssh` heredoc** — fragile and hard to audit; only GitHub secret
  value-masking protects them.

---

## Recommended improvements (not yet applied)

1. Add a `printVersion` task to `build.gradle.kts` (e.g. print `project.version`) or read the version
   directly from the build.
2. Fix the `paths` filters to reference real paths (`infrastructure/docker/docker-compose.yaml`,
   `infrastructure/docker/Dockerfile`, `.github/workflows/*-monolith.yml`).
3. Make the deploy step consistent: use the same compose filename it copies, and use the `docker compose`
   v2 plugin.
4. Fix the heredoc `export` syntax (`=` not `:`).
5. Add PR triggers (`pull_request`) with `concurrency` to gate changes before merge.
6. Enable job-level artifact passing (upload `build/libs/*.jar`, download in `build`) and rely on
   `gradle/actions/setup-gradle` caching for dependencies.
7. Upload test reports as artifacts; add a post-deploy health check (`curl` the `/v3/api-docs` or
   `/actuator/health`) and a rollback path.
8. Declare `PROFILE` / `VERSION` ARGs in the Dockerfile if they are meant to be used, or remove them.

---

## Summary

The pipeline shows correct *intent* (staged build, Docker image push, SSH-based VPS deploy), but the
broken `printVersion` task, mismatched file paths, and deploy inconsistencies mean it will not complete
successfully. Combined with the absence of pull-request gating, caching, and deploy safety, the current
CI/CD setup is best classified as a **work-in-progress draft**, not a mature implementation.
