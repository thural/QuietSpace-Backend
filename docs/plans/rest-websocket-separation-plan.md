# REST / WebSocket Controller Separation Plan

## Objective

Extract all `@MessageMapping` STOMP endpoints from mixed REST+WebSocket controllers into dedicated WebSocket controller classes within each feature domain. Each feature gets its own `controller/` package with both controller types side-by-side. Business logic remains in the shared service layer.

---

## Target Structure

```
src/main/java/dev/thural/quietspace/
├── chat/
│   ├── controller/
│   │   ├── ChatController.java              ← REST only (was mixed)
│   │   └── ChatWebSocketController.java     ← new (@MessageMapping extracted)
│   ├── Chat.java, ChatService.java, dto/    ← unchanged
├── user/
│   ├── controller/
│   │   ├── UserController.java              ← REST only (was mixed)
│   │   ├── AdminController.java             ← REST only (unchanged, move)
│   │   └── UserWebSocketController.java     ← new
├── notification/
│   ├── controller/
│   │   ├── NotificationController.java      ← REST only (was mixed)
│   │   └── NotificationWebSocketController.java  ← new
├── post/controller/PostController.java
├── comment/controller/CommentController.java
├── message/controller/MessageController.java
├── reaction/controller/ReactionController.java
├── photo/controller/PhotoController.java
├── auth/controller/AuthController.java
├── shared/controller/HelloController.java
├── websocket/
│   ├── constant/WebSocketPaths.java         ← new (shared destination constants)
│   ├── config/, event/, model/              ← unchanged
```

---

## Current Violations

| Controller | Current File | REST Endpoints | `@MessageMapping` Endpoints |
|---|---|---|---|
| `ChatController` | `chat/ChatController.java` | 5 | 6 |
| `UserController` | `user/UserController.java` | 14 | 2 |
| `NotificationController` | `notification/NotificationController.java` | 7 | 1 |

### Cross-Package Constant Dependency (must fix first)

`NotificationServiceImpl` (in `notification/`) references:
- `NotificationController.NOTIFICATION_EVENT_PATH`
- `NotificationController.NOTIFICATION_SUBJECT_PATH`

These constants will be moved to `WebSocketPaths`.

---

## Phase 1 — Foundation: Create Packages & Shared Constants

### Step 1.1 — Create per-feature `controller/` packages

```
src/main/java/dev/thural/quietspace/
  chat/controller/
  user/controller/
  notification/controller/
  post/controller/
  comment/controller/
  message/controller/
  reaction/controller/
  photo/controller/
  auth/controller/
  shared/controller/
  websocket/constant/
```

### Step 1.2 — Create `WebSocketPaths.java`

**New file:** `src/main/java/dev/thural/quietspace/websocket/constant/WebSocketPaths.java`

All path constants currently defined in `ChatController`, `UserController`, and `NotificationController` for `@MessageMapping` destinations:

```java
package dev.thural.quietspace.websocket.constant;

public final class WebSocketPaths {

    private WebSocketPaths() {}

    // Chat destinations
    public static final String PUBLIC_CHAT = "/public/chat";
    public static final String PRIVATE_CHAT = "/private/chat";
    public static final String CHAT_EVENT = PRIVATE_CHAT + "/event";
    public static final String LEAVE_CHAT = PRIVATE_CHAT + "/leave";
    public static final String JOIN_CHAT = PRIVATE_CHAT + "/join";
    public static final String DELETE_MESSAGE = PRIVATE_CHAT + "/delete/{messageId}";
    public static final String SEEN_MESSAGE = PRIVATE_CHAT + "/seen/{messageId}";

    // User destinations
    public static final String SET_ONLINE_STATUS = "/user/setOnlineStatus";
    public static final String ONLINE_USERS = "/user/onlineUsers";

    // Notification destinations
    public static final String NOTIFICATION_SUBJECT = "/private/notifications";
    public static final String NOTIFICATION_EVENT = NOTIFICATION_SUBJECT + "/event";
    public static final String NOTIFICATION_SEEN = NOTIFICATION_SUBJECT + "/seen/{notificationId}";

    // Broker destinations
    public static final String USER_PUBLIC = "/user/public";
    public static final String PUBLIC_BROKER = "/public";
}
```

### Step 1.3 — Refactor `NotificationServiceImpl`

Replace:
- `import static dev.thural.quietspace.notification.NotificationController.NOTIFICATION_EVENT_PATH`
  → `import static dev.thural.quietspace.websocket.constant.WebSocketPaths.NOTIFICATION_EVENT`
- `import static dev.thural.quietspace.notification.NotificationController.NOTIFICATION_SUBJECT_PATH`
  → `import static dev.thural.quietspace.websocket.constant.WebSocketPaths.NOTIFICATION_SUBJECT`

---

## Phase 2 — Extract Chat WebSocket Controller

### Step 2.1 — Create `ChatWebSocketController`

**New file:** `src/main/java/dev/thural/quietspace/chat/controller/ChatWebSocketController.java`

| Extracted Method | `@MessageMapping` | `@SendTo` |
|---|---|---|
| `sendMessageToAll(MessageRequest)` | `PUBLIC_CHAT` | `PUBLIC_BROKER + "/chat"` |
| `sendMessageToUser(MessageRequest)` | `PRIVATE_CHAT` | `PUBLIC_CHAT` |
| `deleteMessageById(UUID)` | `DELETE_MESSAGE` | `CHAT_EVENT` |
| `markMessageSeen(UUID)` | `SEEN_MESSAGE` | `CHAT_EVENT` |
| `processLeftChat(ChatEvent)` | `LEAVE_CHAT` | `PUBLIC_CHAT` |
| `processJoinChat(ChatEvent)` | `JOIN_CHAT` | `PUBLIC_CHAT` |

Key changes from original:
- Annotate class with `@Controller` (NOT `@RestController`)
- Remove all manual `SecurityContextHolder` workarounds — `WebSocketConfig.ChannelInterceptor` already handles STOMP CONNECT auth
- Injected deps: `ChatService`, `MessageService`, `MessageRepository`, `UserRepository`

### Step 2.2 — Purge WebSocket artifacts from `ChatController`

- Remove all `@MessageMapping`, `@SendTo`, `@DestinationVariable`, `@Payload` imports
- Remove `MessageRepository`, `UserRepository`, `MessageService` fields (unused after extraction)
- Remove WebSocket path constants (`PUBLIC_CHAT_PATH`, `SOCKET_CHAT_PATH`, etc.)
- Keep `ChatService` (used by REST endpoints)
- Move file from `chat/ChatController.java` → `chat/controller/ChatController.java`
- Update package: `dev.thural.quietspace.chat` → `dev.thural.quietspace.chat.controller`

---

## Phase 3 — Extract User WebSocket Controller

### Step 3.1 — Create `UserWebSocketController`

**New file:** `src/main/java/dev/thural/quietspace/user/controller/UserWebSocketController.java`

| Extracted Method | `@MessageMapping` | `@SendTo` |
|---|---|---|
| `goOffline(UserRepresentation)` | `SET_ONLINE_STATUS` | `USER_PUBLIC` |
| `getOnlineUsers()` | `ONLINE_USERS` | (uses `SimpMessagingTemplate.convertAndSendToUser`) |

Key changes from original:
- Annotate class with `@Controller`
- Injected deps: `UserService`, `SimpMessagingTemplate`

### Step 3.2 — Purge WebSocket artifacts from `UserController`

- Remove `@MessageMapping`, `@SendTo`, `@Payload` imports
- Remove `SimpMessagingTemplate` field
- Remove `ONLINE_USERS_PATH` constant
- Move file from `user/UserController.java` → `user/controller/UserController.java`
- Update package

---

## Phase 4 — Extract Notification WebSocket Controller

### Step 4.1 — Create `NotificationWebSocketController`

**New file:** `src/main/java/dev/thural/quietspace/notification/controller/NotificationWebSocketController.java`

| Extracted Method | `@MessageMapping` |
|---|---|
| `markMessageSeen(UUID)` | `NOTIFICATION_SEEN` |

Key changes from original:
- Annotate class with `@Controller`
- Injected deps: `NotificationService`

### Step 4.2 — Purge WebSocket artifacts from `NotificationController`

- Remove `@MessageMapping`, `@DestinationVariable` imports
- Remove `NOTIFICATION_SUBJECT_PATH`, `NOTIFICATION_EVENT_PATH`, `NOTIFICATION_SEEN_PATH` constants
- Keep `NOTIFICATION_PATH`, `NOTIFICATION_PATH_ID` (used by REST paths and tests)
- Move file from `notification/NotificationController.java` → `notification/controller/NotificationController.java`
- Update package

---

## Phase 5 — Move Remaining Pure REST Controllers

### Step 5.1 — Move to per-feature `controller/` packages

| Current File | New Location |
|---|---|
| `auth/AuthController.java` | `auth/controller/AuthController.java` |
| `post/PostController.java` | `post/controller/PostController.java` |
| `comment/CommentController.java` | `comment/controller/CommentController.java` |
| `message/MessageController.java` | `message/controller/MessageController.java` |
| `reaction/ReactionController.java` | `reaction/controller/ReactionController.java` |
| `photo/PhotoController.java` | `photo/controller/PhotoController.java` |
| `user/AdminController.java` | `user/controller/AdminController.java` |
| `shared/HelloController.java` | `shared/controller/HelloController.java` |

Each requires: package rename + update all cross-file imports referencing it.

### Step 5.2 — Delete old files

After confirming no remaining references, delete all original controller files from domain roots.

---

## Phase 6 — Update Tests

### Step 6.1 — Move test classes to match controller package layout

| Current Test | New Location |
|---|---|
| `test/.../auth/AuthControllerSliceTest.java` | `test/.../auth/controller/AuthControllerSliceTest.java` |
| `test/.../auth/AuthControllerTest.java` | `test/.../auth/controller/AuthControllerTest.java` |
| `test/.../chat/ChatControllerSliceTest.java` | `test/.../chat/controller/ChatControllerSliceTest.java` |
| `test/.../chat/ChatControllerTest.java` | `test/.../chat/controller/ChatControllerTest.java` |
| ... (all 17 test classes follow same pattern) | ... |

### Step 6.2 — Update test imports

- `@WebMvcTest(controllers = XController.class)` → new package
- All `import static XController.CONSTANT` references → update where constants were moved

---

## Phase 7 — Verification

### Step 7.1 — Compile
```bash
./gradlew compileJava
```

### Step 7.2 — Run tests
```bash
./gradlew test
```

---

## File Inventory

### New files (4)
| File | Purpose |
|---|---|
| `websocket/constant/WebSocketPaths.java` | Shared destination constants |
| `chat/controller/ChatWebSocketController.java` | Chat real-time endpoints |
| `user/controller/UserWebSocketController.java` | User presence endpoints |
| `notification/controller/NotificationWebSocketController.java` | Notification real-time endpoints |

### Files to modify (4)
| File | Change |
|---|---|
| `notification/NotificationServiceImpl.java` | Update constant imports |
| `chat/ChatController.java` | Strip WebSocket code, move package |
| `user/UserController.java` | Strip WebSocket code, move package |
| `notification/NotificationController.java` | Strip WebSocket code, move package |

### Files to move (11 controllers + 17 tests = 28 moves)
All controllers from domain roots → `feature/controller/`, tests follow suit.

---

## Execution Order

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7
(foundation)  (chat WS)    (user WS)    (notif WS)   (move REST)   (tests)    (verify)
```

Phases 2–4 are independent and can run in parallel. Phase 5 must follow 2–4. Phase 6 must follow 5.
