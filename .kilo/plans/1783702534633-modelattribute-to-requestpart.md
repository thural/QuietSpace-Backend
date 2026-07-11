# Plan: Convert `@ModelAttribute` upload endpoints to `@RequestPart`

## Context
Two REST endpoints accept file uploads via `@ModelAttribute`, which Springdoc models as a
single **query parameter** referencing the DTO schema. Bruno therefore shows them as "params",
not a request body (the user's reported problem):

- `POST /api/v1/posts` → `PostController.java:83` (`@ModelAttribute PostRequest`)
- `POST /api/v1/messages` → `MessageController.java:28` (`@ModelAttribute MessageRequest`)

Both DTOs contain `MultipartFile photoData` (`PostRequest.java:29`, `MessageRequest.java:32`),
so they are genuinely `multipart/form-data` uploads. The `springdoc.use-fqn=true` fix
(`application.yml:50`) already resolved nested `$ref` issues for `@RequestBody` endpoints; these
two remain because of `@ModelAttribute`.

### Decision (confirmed with user)
Use the **minimal-blast-radius** approach: keep `MultipartFile photoData` inside the DTO.
The controller receives the JSON metadata via `@RequestPart` and the file via a separate
`@RequestPart MultipartFile`, then populates the DTO manually. Add `@JsonIgnore` to `photoData`
so the JSON part schema is clean and the file is represented solely by the separate binary
`photoData` part. The service layer, `ChatController` WebSocket handler (`ChatController.java:95`),
and entity/mapper code are **untouched**.

### Known downstream impact (not in this repo)
This is a **breaking contract change** for the frontend (separate repo). The frontend currently
submits individual form fields (what `@ModelAttribute` expects). After this change it MUST submit
`multipart/form-data` with:
- part `"post"` (or `"messageRequest"`) = `application/json` metadata
- part `"photoData"` = file (optional)

The frontend must be updated in lockstep; otherwise uploads break. This is accepted per prior discussion.

## Steps (commit after each)

### Step 1 — Exclude `photoData` from the JSON schema
- In `PostRequest.java` and `MessageRequest.java`, add `@JsonIgnore` to the `photoData` field
  (keep `@Getter/@Setter`, field, and imports; `MultipartFile` import stays).
- Reason: when the metadata is sent as a JSON `@RequestPart`, `photoData` must not appear in that
  JSON schema; the file is delivered via the separate binary part. `@JsonIgnore` keeps the field
  usable for `setPhotoData(...)`/service reads while hiding it from JSON.
- Commit.

### Step 2 — Convert `PostController.createPost`
- Change signature (PostController.java:82-85) to:
  ```java
  @PostMapping
  ResponseEntity<PostResponse> createPost(
          @RequestPart @Valid PostRequest post,
          @RequestPart(value = "photoData", required = false) MultipartFile photoData) {
      post.setPhotoData(photoData);
      return ResponseEntity.ok(postService.addPost(post));
  }
  ```
- Add import `org.springframework.web.multipart.MultipartFile`. Keep method-level `@Validated`
  (harmless). `@Valid` on the `@RequestPart` DTO preserves the existing bean-validation rules.
- Commit.

### Step 3 — Convert `MessageController.createMessage`
- Apply the identical pattern (MessageController.java:27-30):
  ```java
  @PostMapping
  ResponseEntity<MessageResponse> createMessage(
          @RequestPart @Valid MessageRequest messageRequest,
          @RequestPart(value = "photoData", required = false) MultipartFile photoData) {
      messageRequest.setPhotoData(photoData);
      return ResponseEntity.ok(messageService.addMessage(messageRequest));
  }
  ```
- Add `MultipartFile` import. Commit.

### Step 4 — Update controller tests
- `src/test/.../slice/PostControllerTest.java` and `.../unit/PostControllerTest.java`: rewrite
  `createPost` to use `MockMultipartFile` — a `"post"` JSON part (`objectMapper.writeValueAsString(postRequest)`)
  plus an optional `"photoData"` `MockMultipartFile`; perform via `mockMvc.perform(multipart(POST_PATH).file(...))`.
  Add a no-file case and an invalid-payload (400) case.
- Same for `MessageControllerTest` slice + unit (`createMessage`, part name `"messageRequest"`).
- Service tests (`PostServiceImplTest`, `MessageServiceImplTest`) need **no** change — they call
  `addPost`/`addMessage` directly and still set `photoData` via the builder.
- Commit.

### Step 5 — Build, verify spec, smoke-test
- `./gradlew build` (or `test`) must pass.
- Start app (per `docs/usage-guide.md` Option A: `set -a && . ./.env && set +a; ./gradlew bootRun --no-daemon`).
- Fetch `http://localhost:8080/v3/api-docs` and confirm `POST /api/v1/posts` and `POST /api/v1/messages`
  now expose `requestBody` with `multipart/form-data` containing two parts: the JSON metadata DTO
  (no `photoData` field) and a `photoData` binary part.
- Commit only if any follow-up tweak is required.

## Risks / validation
- **Frontend breakage** is expected and accepted; coordinate the frontend change separately.
- Ensure `photoData` stays `required = false` (uploads are optional) — matches current behavior.
- Verify validation still returns 400 on missing `@NotNull`/`@NotBlank` fields (e.g. `userId`, `text`).
- Confirm `ChatController` WebSocket path is unaffected (it passes `MessageRequest` without a file;
  `photoData` remains null there, same as before).
