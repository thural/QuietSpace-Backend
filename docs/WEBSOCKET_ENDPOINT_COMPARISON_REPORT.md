# WebSocket (Real-Time) Endpoint Comparison Report

**FastAPI (Socket.IO) vs Spring Boot (STOMP) — Enterprise Social Media Real-Time Architecture Best Practices**

---

## Assessment Criteria

Best practices for 2026 enterprise real-time APIs:

| Criterion | Description |
|-----------|-------------|
| **Protocol maturity** | Ecosystem support, client libraries, documentation |
| **Transport fallback** | Graceful degradation when WebSocket is blocked |
| **Authentication** | Secure handshake, token validation, session management |
| **Event naming** | Clear, discoverable, self-documenting event names |
| **Payload typing** | Strongly typed, versioned message schemas |
| **Room/scoping** | Efficient channel isolation for multi-tenant data |
| **Error handling** | Structured error events, reconnection strategies |
| **Scalability** | Horizontal scaling with pub/sub backplane |
| **Reconnection** | Automatic reconnection with backoff, state recovery |
| **Documentation** | AsyncAPI spec, generated client SDKs, interactive docs |
| **Observability** | Logging, tracing, health checks for WS connections |

---

## 1. Architectural Comparison

| Aspect | FastAPI (Socket.IO) | Spring Boot (STOMP) | Best Practice |
|--------|---------------------|---------------------|---------------|
| **Protocol** | Socket.IO (`python-socketio` 5.13) | STOMP over WebSocket (Spring WebSocket) | **Tie** — both are mature |
| **Transport** | WebSocket + HTTP long-polling fallback (built-in) | WebSocket + SockJS fallback | **Socket.IO** — built-in, no extra config |
| **Message format** | Named JSON events | STOMP frames with JSON payload | **Socket.IO** — simpler, no frame overhead |
| **Pub/sub backplane** | Redis (`AsyncRedisManager`) | SimpMessagingTemplate + broker relay | **Tie** — both support Redis pub/sub |
| **Room model** | Application-level (`socketio.enter_room`) | Broker-level prefixes (`/user/`, `/public/`, `/private/`) | **Socket.IO** — more flexible, not tied to path structure |
| **Auth** | JWT in `HTTP_AUTHORIZATION` header during handshake | JWT in STOMP `CONNECT` frame header | **Socket.IO** — cleaner, reuses HTTP auth patterns |
| **Error handling** | Implicit via Socket.IO error events | Explicit `_error` STOMP channel | **Legacy** — explicit error channel is better for debugging |
| **Client library** | `socket.io-client` (JS/TS/Python/Swift/Java/Kotlin) | STOMP.js, RxStomp (JS/TS only) | **Socket.IO** — broader language support |
| **Fallback for restricted networks** | HTTP long-polling (automatic) | SockJS (manual config) | **Socket.IO** — seamless, no extra dependency |
| **Reconnection** | Automatic with exponential backoff (built-in) | Manual via client config | **Socket.IO** — built-in and battle-tested |

### 1.1 Winner: Socket.IO (FastAPI) — Architectural Level

Socket.IO is the stronger choice for a social media application in 2026 because:

1. **Universal client support** — First-class libraries for JavaScript, Swift, Kotlin/Java (mobile!), Python, and C++. STOMP is primarily a web-only technology.
2. **Automatic reconnection** — Built-in exponential backoff, event buffering during disconnection, and idempotent event delivery. STOMP requires manual reconnection logic.
3. **Transport fallback** — Falls back to HTTP long-polling when WebSocket is unavailable (corporate firewalls, proxies). SockJS achieves the same but requires more configuration.
4. **Simpler message model** — Named JSON events vs STOMP frames. Less overhead per message, easier to debug and document.
5. **Redis adapter** — `AsyncRedisManager` provides out-of-the-box multi-process scaling identical to how Spring STOMP does it.

---

## 2. Per-Event Comparison

### 2.1 Connection & Authentication

#### `connect` (Socket.IO Client → Server)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | `connect` (Socket.IO lifecycle) | STOMP `CONNECT` frame |
| **Auth mechanism** | `HTTP_AUTHORIZATION` handshake header | STOMP CONNECT frame `Authorization` header |
| **Validation** | JWT decoded, user fetched from DB | JWT extracted via `CustomHandshakeInterceptor` |
| **Room assignment** | Enters `public` room automatically | Subscriptions managed client-side to `/public` |

**Winner: FastAPI** — Simpler, reuses standard HTTP header for auth. The legacy custom handshake interceptor adds unnecessary complexity.

**Improvement:** Add a `connection_error` event that sends structured error payloads (`{ code, message, retry_after }`) when auth fails, rather than silently dropping the connection.

---

#### `disconnect` (Socket.IO Client → Server)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | `disconnect` (Socket.IO lifecycle) | STOMP `DISCONNECT` frame |
| **Cleanup** | Removes user from `active_connections`, deletes from Redis `online_users` | Sets user to `OFFLINE` in DB |
| **Broadcast** | `user_disconnected` to all clients | `BaseEvent{CONNECT}` (wait, DISCONNECT) to `/public` |

**Winner: FastAPI** — Redis-based online tracking is faster and more scalable than DB writes on every disconnect.

**Improvement:** Implement a heartbeat mechanism to detect stale connections (clients that disconnected without sending the `disconnect` event). Socket.IO supports this via `ping_interval` and `ping_timeout` settings.

---

### 2.2 User Presence

#### `set_online_status` / `user_status` (FastAPI) vs `/app/user/setOnlineStatus` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `set_online_status` with `{ user_id, status }` | STOMP `/app/user/setOnlineStatus` |
| **Server broadcasts** | `user_status` to chat room members | `convertAndSendToUser("/user/public", ...)` |
| **Status granularity** | `status` field (string, supports any value) | `UserRepresentation` (likely online/offline only) |
| **Who receives** | All participants of user's chats | Only current user's followers/subscribers |

**Winner: FastAPI** — More flexible status values (`online`, `away`, `busy`, etc.) vs legacy binary online/offline.

**Improvement:** Add `last_seen_at` timestamp to the payload. Consider `GET /api/v1/users/online` which already exists. Ensure the WS event and REST endpoint return consistent data.

---

#### `get_online_users` / `online_users` (FastAPI) vs `/app/user/onlineUsers` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client request** | `get_online_users` (no payload) | STOMP `/app/user/onlineUsers` |
| **Server response** | `online_users` with `{ online_users: [UUID] }` | `convertAndSendToUser("/user/onlineUsers", ...)` |
| **Scope** | All online users | User's followings only |

**Winner: Legacy** — Scoping to followings is more privacy-conscious and useful for social media. A list of ALL online users is noisy and potentially a privacy concern.

**Improvement:** Change FastAPI to return only the requesting user's followings' online status. The REST endpoint `GET /api/v1/users/online` already exists for the global view if needed; the WS event should be scoped.

---

#### `user_connected` / `user_disconnected` (FastAPI) vs SessionConnectEvent / SessionDisconnectEvent (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Trigger** | In `manager.connect_user()` / `disconnect_user()` | Spring `SessionConnectEvent` / `SessionDisconnectEvent` listener |
| **Payload** | `{ user_id }` | `BaseEvent{ type: CONNECT/DISCONNECT, timestamp, actor_id }` |
| **Audience** | All connected clients (global broadcast) | Subscribers of `/public` |

**Winner: Legacy** — Using a structured `BaseEvent` envelope with `event_type` and `timestamp` is better for clients that need to handle events generically. FastAPI's bare `{ user_id }` is minimal.

**Improvement:** Wrap the payload in a typed event envelope: `{ event_type: "USER_CONNECTED", user_id, timestamp, data: {} }`. This allows clients to use a generic event handler.

---

### 2.3 Chat & Messaging

#### `join_chat` (FastAPI) vs `/app/private/chat/join` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `join_chat` with `{ user_id, chat_id }` | STOMP `/app/private/chat/join` |
| **Server action** | Enters `chat_{chat_id}` room, broadcasts `chat_event{JOIN_CHAT}` | Enters chat room, sends `ChatEvent{JOINED_CHAT}` |
| **Event channel** | `chat_event` with `event_type` discriminator | `/user/private/chat/event` |

**Winner: FastAPI** — Using a single `chat_event` channel with a typed discriminator (`JOIN_CHAT`, `LEAVE_CHAT`, etc.) is cleaner than separate STOMP destinations for each event type.

**Improvement:** Validate that the requesting user is actually a member of the chat before allowing them to join the room. This is a security check the legacy handles at the service layer.

---

#### `leave_chat` (FastAPI) vs `/app/private/chat/leave` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `leave_chat` with `{ user_id, chat_id }` | STOMP `/app/private/chat/leave` |
| **Server action** | Leaves `chat_{chat_id}` room, broadcasts `chat_event{LEAVE_CHAT}` | Sends `ChatEvent{LEFT_CHAT}` |

**Winner:** Tie — functionally identical.

**Improvement:** Broadcast `chat_event{LEAVE_CHAT}` to the leaving user as well, so they can update their local UI state.

---

#### `send_message` (FastAPI) vs `/app/private/chat` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `send_message` with `{ chat_id, sender_id, recipient_id, text }` | STOMP `/app/private/chat` |
| **Server action** | Persists message, emits `new_message` to sender + recipient, `message_in_chat` to room | Persists, sends to sender + recipient via `convertAndSendToUser`, broadcasts to `/public/chat` |
| **Delivery channels** | `new_message` (individual) + `message_in_chat` (room) | `/user/private/chat` (individual) + `/public/chat` (room) |
| **Persistence** | Persisted before emit | Likely persisted before emit |

**Winner:** Tie — both persist-first-then-emit, which is the correct pattern.

**Improvement:** Include a client-generated `message_id` (UUID) in the request so the client can deduplicate on reconnection. Socket.IO's event buffer can cause duplicate sends after a temporary disconnection.

---

#### `delete_message` (FastAPI) vs `/app/private/chat/delete/{messageId}` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `delete_message` with `{ message_id, user_id, chat_id }` | STOMP `/app/private/chat/delete/{messageId}` |
| **Server action** | Soft-deletes, broadcasts `chat_event{DELETE_MESSAGE}` | Same |
| **Event channel** | `chat_event` with `event_type: DELETE_MESSAGE` | `/user/private/chat/event` |

**Winner:** Tie — functionally identical.

**Improvement:** Validate that the requesting `user_id` is the original message sender before allowing deletion. The legacy likely does this in the service layer; verify FastAPI does too.

---

#### `seen_message` (FastAPI) vs `/app/private/chat/seen/{messageId}` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | `seen_message` with `{ message_id, user_id, chat_id }` | STOMP `/app/private/chat/seen/{messageId}` |
| **Server action** | Marks as read, broadcasts `chat_event{SEEN_MESSAGE}` | Same |
| **Event channel** | `chat_event` with `event_type: SEEN_MESSAGE` | `/user/private/chat/event` |

**Winner:** Tie — functionally identical.

**Improvement:** Include `read_at` timestamp in the `chat_event` payload so clients can display precise read timestamps.

---

#### `new_message` / `message_in_chat` (FastAPI) vs `/user/private/chat` / `/public/chat` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Room notification** | `message_in_chat` to `chat_{id}` room | `/public/chat` broadcast (all subscribers) |
| **Individual delivery** | `new_message` to sender + recipient individually | `convertAndSendToUser("/user/private/chat")` to sender + recipient |

**Winner: FastAPI** — Using a dedicated room per chat (`chat_{chat_id}`) is superior to a shared `/public/chat` destination because:
1. Clients don't need to filter out messages from chats they're not in
2. Better security isolation
3. Less bandwidth — clients only receive what they need

**Improvement:** None needed — this is a strong design.

---

### 2.4 Notifications

#### `notification` (FastAPI) vs `/user/private/notifications` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | `notification` with `NotificationEvent` payload | STOMP `/user/private/notifications` |
| **Payload** | `{ event_type, timestamp, actor_id, data, notification_id, notification_type, recipient_id }` | Likely similar notification DTO |
| **Trigger** | Created in `NotificationService.create_notification()` | Created in `NotificationController` |

**Winner:** Tie — both use structured event payloads with typed notification data.

**Improvement:** Ensure the payload includes a `created_at` timestamp and the notification `type` as a first-class field (not buried in `data`).

---

#### `unread_count` (FastAPI) vs Legacy

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Endpoint** | ✅ `unread_count` event with `{ count }` | ❌ Not available via WebSocket |

**Winner: FastAPI** — Dedicated unread count push is essential for badge updates on mobile.

**Improvement:** Include a `total_count` and per-type breakdown: `{ total, by_type: { MESSAGE: 3, LIKE: 5 } }`.

---

#### `new_notification` (FastAPI) vs Legacy

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | `new_notification` with raw `Notification` model | (Part of `/user/private/notifications`) |
| **Purpose** | Raw notification data for client-side rendering | Same |

**Winner:** Tie — same functionality, different naming.

**Improvement:** Consider whether `new_notification` and `notification` can be consolidated into a single event. Having both is confusing — one should be deprecated in favor of the other.

---

#### Notification Read (FastAPI) vs `/app/private/notifications/seen/{notificationId}` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Client sends** | (via REST `PUT /notifications/{id}/read`) | STOMP `/app/private/notifications/seen/{notificationId}` |
| **Server pushes** | `notification` event with `NOTIFICATION_READ` type (via `EventFactory`) | (presumably sends updated notification) |

**Winner: FastAPI** — REST is the correct choice for marking notifications as read because it's a simple CRUD operation that benefits from HTTP semantics (caching, idempotency, status codes). WebSocket should be reserved for real-time pushes, not writes.

**Improvement:** After the REST call marks as read, push the updated `unread_count` event over WebSocket to update badge counts in real-time. This is already implemented.

---

### 2.5 Public Chat

#### `public_message` (FastAPI) vs `/app/public/chat` ↔ `/public/chat` (Legacy)

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Send** | `public_message` with `{ user_id, message, type }` | STOMP `/app/public/chat` |
| **Receive** | Same `public_message` event (broadcast to `public` room) | `/public/chat` (broker broadcast) |
| **Payload** | `{ user_id, message, type }` | (similar message payload) |

**Winner:** Tie — functionally identical.

**Improvement:** Add message persistence for public chat so history is available on reconnect. Currently public messages are fire-and-forget.

---

### 2.6 System Events

#### `system` (FastAPI) vs Legacy

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | ✅ `system` with `SystemEvent` payload | ❌ Not available |

**Winner: FastAPI** — System event broadcasting is important for maintenance notifications, policy updates, and server-side announcements.

**Improvement:** Add a `severity` field (already in the `SystemEvent` schema) and consider a `system` topic with channel-based subscription (e.g., only admins receive critical system events).

---

### 2.7 Typing Indicator

#### `typing_status` (FastAPI) vs Legacy

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Event** | ✅ `typing_status` with `{ user_id, is_typing }` | ❌ Not available |

**Winner: FastAPI** — Typing indicators are table stakes for modern chat applications. Their absence in the legacy is a significant gap.

**Improvement:** Add a `chat_id` field to the payload so the receiving client knows which chat the typing event belongs to (currently broadcast to the chat room, so the room context provides this — but explicit is better).

---

### 2.8 Error Events

#### Legacy `_error` Channel vs FastAPI

| Aspect | FastAPI | Legacy |
|--------|---------|--------|
| **Error channel** | ❌ No explicit error event | ✅ `_error` STOMP channel |
| **Error payload** | Socket.IO internal error handling only | Structured error frame with code + message |

**Winner: Legacy** — Having a dedicated error channel is better for debugging and client-side error handling. Socket.IO errors are often opaque or lost in internal handling.

**Improvement:** Add a documented `error` event that the server emits when operations fail. Payload should include: `{ code: string, message: string, operation: string, timestamp }`.

---

## 3. Best Practices Assessment

### 3.1 What FastAPI Does Well

| Practice | Rating | Evidence |
|----------|--------|----------|
| **Event naming** | ✅ **Excellent** | All event names are clear, lowercase, snake_case verbs |
| **Payload typing** | ✅ **Excellent** | Pydantic models (`BaseEvent`, `ChatEvent`, `NotificationEvent`, `SystemEvent`) with discriminated types |
| **Room isolation** | ✅ **Excellent** | Per-chat rooms (`chat_{chat_id}`) for message delivery |
| **Redis pub/sub** | ✅ **Excellent** | `AsyncRedisManager` for horizontal scaling |
| **Auth** | ✅ **Excellent** | JWT in handshake header, validated on every connect |
| **Documentation** | ✅ **Excellent** | Full AsyncAPI 2.6 spec with typed schemas |

### 3.2 What Could Be Improved

| Practice | Rating | Issue |
|----------|--------|-------|
| **Error events** | ❌ **Missing** | No documented error event for operation failures |
| **Event envelope** | ⚠️ **Inconsistent** | Some events use typed envelopes (`chat_event`), others are bare payloads (`user_connected` sends only `{ user_id }`) |
| **Heartbeat** | ⚠️ **Unclear** | Socket.IO has built-in ping/pong, but verify it's configured explicitly |
| **Idempotency** | ⚠️ **Not addressed** | No client-generated message IDs for deduplication |
| **Event versioning** | ❌ **Missing** | No `event_version` field in payloads for schema evolution |
| **Rate limiting** | ❌ **Missing** | No rate limiting on WebSocket event publishing |

### 3.3 What Legacy Does Well

| Practice | Rating | Evidence |
|----------|--------|----------|
| **Error channel** | ✅ **Good** | `_error` STOMP channel for structured error delivery |
| **Structured envelopes** | ✅ **Good** | `BaseEvent` with type, timestamp, actor_id on all events |
| **Scope filtering** | ✅ **Good** | Online users scoped to followings |

### 3.4 What Legacy Does Poorly

| Practice | Rating | Issue |
|----------|--------|-------|
| **Client support** | ❌ **Limited** | STOMP is primarily web-only; no first-class mobile SDK |
| **Reconnection** | ❌ **Manual** | No built-in reconnection with state recovery |
| **Typing indicators** | ❌ **Missing** | Not implemented |
| **System events** | ❌ **Missing** | Not implemented |
| **Unread count push** | ❌ **Missing** | Not implemented |
| **Documentation** | ⚠️ **Partial** | SpringWolf AsyncAPI spec present but channels named opaquely (underscore-prefixed) |

---

## 4. Improvement Plan for FastAPI WebSocket Layer

### 4.1 High Priority

| # | Improvement | Current State | Recommendation |
|---|-------------|---------------|----------------|
| 1 | **Add error event** | No error event documented | Add a server-emitted `error` event: `{ code, message, operation, timestamp }`. Emit on operation failures (delete non-owned message, join non-member chat, etc.) |
| 2 | **Consistent event envelope** | Mix of typed envelopes and bare payloads | Standardize all server-emitted events to use a typed envelope: `{ event_type, timestamp, payload }`. Currently `user_connected` sends `{ user_id }` while `chat_event` sends the full `ChatEvent` model. |
| 3 | **Heartbeat configuration** | Socket.IO defaults may not be tuned for mobile | Explicitly configure `ping_interval` and `ping_timeout` in the `AsyncServer` constructor to handle mobile network interruptions (recommended: 25s / 60s). |
| 4 | **Client message ID** | No dedup support | Add `client_message_id: UUID` as optional field in `send_message`, `delete_message`, `seen_message`. Server should ignore duplicates within a TTL window. |

### 4.2 Medium Priority

| # | Improvement | Current State | Recommendation |
|---|-------------|---------------|----------------|
| 5 | **Online users scope** | Returns all online users | Change `get_online_users` to return only the requesting user's followings. Use the existing repository/follow service. |
| 6 | **Event versioning** | No version field | Add `event_version: int` to all event envelopes. Start at 1. Increment when schemas change. This allows clients to handle multiple schema versions during rolling upgrades. |
| 7 | **Rate limiting** | No WS rate limits | Add per-user rate limiting on publish events (e.g., max 10 `send_message` events per second). `slowapi` won't apply to WS — implement a token bucket using Redis. |
| 8 | **latency measurement** | No WS performance tracking | Add client-side `client_timestamp` to publish payloads. Server includes `server_timestamp` in response events. Clients can calculate round-trip latency. |

### 4.3 Low Priority

| # | Improvement | Current State | Recommendation |
|---|-------------|---------------|----------------|
| 9 | **Public chat persistence** | Fire-and-forget | Persist public messages to DB (same `Message` model, with a virtual `chat_id` for the public room). Provide history on reconnect. |
| 10 | **Typing throttling** | No rate limit on typing events | Implement server-side throttling — only broadcast `typing_status` at most once per 2s per user per chat. Clients send on every keystroke but server deduplicates. |
| 11 | **Consolidate notification events** | Both `notification` and `new_notification` | Merge into a single `notification` event. The `NotificationEvent` envelope already has the `event_type` discriminator to distinguish CREATE vs READ. |

---

## 5. Cross-Cutting Recommendations

### 5.1 AsyncAPI Spec Updates

| Item | Current | Recommendation |
|------|---------|----------------|
| Document `error` event | ❌ Missing | Add subscribe channel for `error` with typed payload schema |
| Add `event_version` to schemas | ❌ Missing | Add property to `BaseEvent` schema |
| Document heartbeat | ❌ Missing | Add `x-heartbeat-interval` extension to the server object |

### 5.2 Connection Lifecycle

```
Current flow:
  Client → connect (with JWT)
  Server → connected { user_id }
  Client ↔ [events]
  Client → disconnect
  Server (detects via cleanup)

Improved flow:
  Client → connect (with JWT + client_version)
  Server → connected { user_id, server_time, heartbeat_interval }
  Server → error { code: "AUTH_EXPIRED" } (if token will expire soon)
  Server ↔ ping/pong (automatic, configured interval)
  Client → disconnect
  Server → user_disconnected { user_id, last_seen }
```

### 5.3 Comparison to Industry Standards

| Feature | FastAPI | Legacy | Discord | Slack | WhatsApp |
|---------|---------|--------|---------|-------|----------|
| **Typing indicators** | ✅ | ❌ | ✅ | ✅ | ✅ |
| **Read receipts** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Error events** | ❌ | ✅ | ✅ | ✅ | ✅ |
| **Event schemas** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Client dedup IDs** | ❌ | ❌ | ✅ | ✅ | ✅ |
| **Rate limiting** | ❌ | ❌ | ✅ | ✅ | ✅ |
| **Public chat** | ✅ | ✅ | N/A | N/A | N/A |

---

## 6. Summary

| Dimension | Verdict |
|-----------|---------|
| **Protocol choice** | ✅ **FastAPI** — Socket.IO is the right choice; broader client support, built-in reconnection and fallback |
| **Event design** | ✅ **FastAPI** — Clean event names, typed payloads, discriminated chat events |
| **Room/scoping** | ✅ **FastAPI** — Per-chat rooms superior to shared broker destinations |
| **Error handling** | ❌ **Legacy** — Legacy's explicit error channel is better; FastAPI should add one |
| **Online presence** | ⚠️ Mixed — FastAPI architecture better, but scoping to followings would be ideal |
| **Notifications** | ✅ **FastAPI** — Dedicated unread count push is a key advantage |
| **Missing features (FastAPI)** | Error events, client dedup IDs, event versioning, WS rate limiting |
| **Missing features (Legacy)** | Typing indicators, system events, unread count push, mobile SDK support |
| **Overall** | **FastAPI is architecturally superior** — better protocol, cleaner event model, more features. The main gaps (error events, dedup IDs, WS rate limiting) are straightforward to add. |
