# API Improvement Plan

Based on comparison with the Python/FastAPI branch, this plan fills gaps and improves REST semantics for the Spring Boot backend.

---

## Phase 1 — Missing REST Endpoints (High Impact)

Add new endpoints that exist in FastAPI but are missing here.

### Step 1.1: Chat — `GET /api/v1/chats`
- **What**: List the signed-in user's chats
- **Files**: `ChatController.java` — add `getMyChats()` calling `userService.getSignedUser().getId()` then `chatService.getChatsByUserId()`
- **Service**: Already exists (`ChatService.getChatsByUserId`)

### Step 1.2: Chat — `PATCH /api/v1/chats/{chatId}`
- **What**: Update chat metadata (name, avatar)
- **Files**: Add `updateChat()` method to `ChatService` + `ChatServiceImpl`, add endpoint in `ChatController`

### Step 1.3: Messages — `GET /api/v1/messages/unread`
- **What**: Get unread message count for signed-in user
- **Files**: Add `countByChatIdAndIsSeen()` to `MessageRepository`, add `getUnreadCount()` to `MessageService`/`MessageServiceImpl`, add endpoint in `MessageController`

### Step 1.4: Messages — `PUT /api/v1/messages/{messageId}/read`
- **What**: Mark message as read via REST
- **Files**: Add endpoint in `MessageController` calling existing `messageService.setMessageSeen()`

### Step 1.5: User — `GET /api/v1/users/me`
- **What**: Get current user profile (alias for `/profile` with standard `/me` pattern)
- **Files**: Add endpoint in `UserController` calling `userService.getLoggedUserResponse()`

### Step 1.6: User — `PATCH /api/v1/users/me`
- **What**: Update current user (alias for current `PATCH /api/v1/users` with explicit `/me` target)
- **Files**: Add endpoint in `UserController`

### Step 1.7: User — `POST /api/v1/users/{userId}/follow`
- **What**: Follow a user (idempotent, no-op if already following)
- **Files**: Add `followUser()` to `UserService`/`UserServiceImpl`, add endpoint in `UserController`

### Step 1.8: User — `DELETE /api/v1/users/{userId}/follow`
- **What**: Unfollow a user (idempotent, no-op if not following)
- **Files**: Add `unfollowUser()` to `UserService`/`UserServiceImpl`, add endpoint in `UserController`

### Step 1.9: User — `DELETE /api/v1/users/profile/block/{userId}`
- **What**: Unblock a user
- **Files**: Add `removeUserFromBlockList()` to `UserService`/`UserServiceImpl`, add endpoint in `UserController`

### Step 1.10: User — `GET /api/v1/users/profile/blocked`
- **What**: List blocked users
- **Files**: Add `getBlockedUsers()` to `UserService`/`UserServiceImpl`, add endpoint in `UserController`

### Step 1.11: User — `GET /api/v1/users/online`
- **What**: Get online followings
- **Files**: Add endpoint in `UserController` calling `userService.findConnectedFollowings()`

### Step 1.12: Notifications — `PUT /api/v1/notifications/{notificationId}/read`
- **What**: Mark individual notification as read (granular)
- **Files**: Add endpoint in `NotificationController` calling `notificationService.handleSeen()`
- **Note**: Legacy has `POST /notifications/seen/{contentId}` which marks all by content — keep both

### Step 1.13: Reactions — `GET /api/v1/reactions/post/{postId}`
- **What**: Get reactions for a specific post
- **Files**: Add to `ReactionRepository`, `ReactionService`, `ReactionController`

### Step 1.14: Photos — `POST /api/v1/photos` + `DELETE /api/v1/photos/{photoId}`
- **What**: Generic photo upload + delete by ID
- **Files**: Add to `PhotoService`/`PhotoController`

### Step 1.15: Admin — `PUT /api/v1/admin/users/{userId}/disable`
- **What**: Disable user account
- **Files**: Add `disableUser()` to `UserService`/`UserServiceImpl`, add endpoint in `AdminController`

### Step 1.16: Health — `GET /health`
- **What**: Component-level health check
- **Files**: Add `HealthController` in `shared/controller/`

---

## Phase 2 — REST Semantic Improvements (Medium Impact)

Improve existing endpoints for REST best practices without breaking current clients (add new endpoints alongside old ones).

### Step 2.1: Auth — `POST /api/v1/auth/login` (alias)
- Add as alias for `/authenticate`

### Step 2.2: Auth — `POST /api/v1/auth/logout` (alias)
- Add as alias for `/signout`

### Step 2.3: Auth — `POST /api/v1/auth/refresh` (alias)
- Add as alias for `/refresh-token`

### Step 2.4: Posts — `POST /api/v1/posts/{postId}/save` + `DELETE /api/v1/posts/{postId}/save`
- Replace `PATCH /posts/saved/{postId}` toggle with proper POST/DELETE

### Step 2.5: Reactions — `POST /api/v1/reactions` + `DELETE /api/v1/reactions/{reactionId}`
- Replace `POST /reactions/toggle-reaction` with separate add/remove

### Step 2.6: Admin — `DELETE /api/v1/admin/users/{userId}`
- Replace `POST /api/v1/admin/{userId}` with proper DELETE

### Step 2.7: Followers — `DELETE /api/v1/users/{userId}/followers/{followerId}`
- Replace `POST /users/followers/remove/{userId}` with DELETE

---

## Phase 3 — WebSocket Features (High Impact)

Add real-time features from FastAPI that are missing.

### Step 3.1: Typing indicator — `typing_status` event
- Add `@MessageMapping` in `ChatWebSocketController`
- Payload: `{ userId, chatId, isTyping }`

### Step 3.2: Unread count push
- Push `unread_count` event over WS after notification/message changes

### Step 3.3: System events
- Add `system` event channel for maintenance/announcements

### Step 3.4: Error events
- Add documented `error` event for operation failures

---

## Execution

1. Each step = a `git add -A && git commit`
2. Pause after each commit for confirmation
3. Verify with `./gradlew compileJava` before each commit
