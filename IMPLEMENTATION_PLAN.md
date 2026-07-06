# Implementation Plan: FastAPI Feature Parity

**Based on:** FUNCTIONAL_PARITY_ANALYSIS.md  
**Created:** July 6, 2026  
**Objective:** Achieve functional parity with legacy Spring Boot backend using Socket.IO

---

## Overview

This implementation plan addresses missing features identified in the functional parity analysis. The plan is organized into phases prioritizing critical functionality first, with concrete steps for each feature implementation.

**Technology Constraints:**
- Continue using Socket.IO for WebSocket communication (not STOMP)
- Maintain existing FastAPI architecture
- Follow existing code patterns and conventions

**Architectural Principles:**
- **Async-First:** All WebSocket handlers MUST use async database operations (AsyncSession with asyncpg)
- **Cursor-Based Pagination:** Use cursor-based pagination for social feeds (Posts, Notifications, Messages)
- **Unit of Work Pattern:** Implement transaction management with context managers for multi-step operations
- **Soft Deletions:** Use soft deletes for messages and other critical data (is_deleted/deleted_at)
- **Redis-Backed Rate Limiting:** Use middleware (slowapi) for rate limiting, not service-level checks

---

## Phase 0: Foundation & Architecture (Week 1)

### 0.1 Structured Event System & Redis Setup

**Objective:** Establish event-driven architecture and Redis pub/sub infrastructure before implementing WebSocket features

**Steps:**

1. **Base Event Class**
   - Create `app/models/websocket_events.py` with BaseEvent class
   - Define common event fields: type, timestamp, actor_id, data

2. **Event Subclasses**
   - Create ChatEvent, NotificationEvent, SystemEvent subclasses
   - Implement proper serialization/deserialization

3. **Event Factory**
   - Create event factory for creating typed events
   - Ensure type safety and validation

4. **Redis Configuration**
   - Verify Redis configuration for Socket.IO scaling
   - Test Redis pub/sub functionality
   - Configure connection pooling

5. **Unit of Work Pattern**
   - Create `app/core/unit_of_work.py` with UnitOfWork context manager
   - Implement transaction lifecycle management
   - Ensure all service layers use UnitOfWork for multi-step operations

6. **Async Database Driver Verification**
   - Verify asyncpg is configured for all database operations
   - Ensure all repositories use AsyncSession
   - Add validation to prevent blocking calls in WebSocket handlers

**Acceptance Criteria:**
- Event system is structured and type-safe
- Redis pub/sub is configured and tested
- Unit of Work pattern is implemented and tested
- All database operations use async drivers

---

### 0.2 Cursor-Based Pagination Foundation

**Objective:** Implement cursor-based pagination infrastructure for social feeds

**Steps:**

1. **Pagination Schema**
   - Create `app/schemas/pagination.py` with:
     - CursorRequest schema (cursor: str, limit: int)
     - CursorResponse schema (items: list, next_cursor: str, has_more: bool)
   - Create OffsetPaginationResponse for admin/static endpoints

2. **Cursor Utilities**
   - Create `app/utils/cursor.py` with cursor encoding/decoding utilities
   - Implement cursor generation from (timestamp + id) tuples
   - Add base64 encoding for opaque cursors

3. **Repository Base Pattern**
   - Update base repository pattern to support both cursor and offset pagination
   - Document which endpoints should use cursor vs offset

4. **Migration Guidelines**
   - Document migration path from offset to cursor pagination
   - Create helper functions for cursor-based queries

**Acceptance Criteria:**
- Cursor utilities are implemented and tested
- Pagination schemas support both cursor and offset patterns
- Repository pattern supports both pagination types
- Migration guidelines are documented

---

## Phase 1: Critical Social Features (Weeks 2-3)

### 1.1 User Blocking System

**Objective:** Enable users to block other users for safety and content moderation

**Steps:**

1. **Database Schema Updates**
   - Add `blocked_users` table with columns: `id`, `blocker_id`, `blocked_id`, `created_at`
   - Add unique constraint on `(blocker_id, blocked_id)`
   - Create Alembic migration script

2. **Model Implementation**
   - Create `app/models/blocked_user.py` with BlockedUser model
   - Define relationships with User model

3. **Repository Implementation**
   - Create `app/repositories/blocked_user.py` with BlockedUserRepository
   - Implement methods: `block_user()`, `unblock_user()`, `is_blocked()`, `get_blocked_users()`
   - **CRITICAL:** All methods must use AsyncSession and async operations

4. **Schema Implementation**
   - Create `app/schemas/blocked_user.py` with BlockUserRequest, BlockedUserResponse schemas

5. **Service Implementation**
   - Create `app/services/block_service.py` with BlockService
   - Implement blocking logic with validation (cannot block self, check if already blocked)
   - **Use Unit of Work pattern** for multi-step operations (block + remove reciprocal follows)

6. **API Endpoint**
   - Add endpoint to `app/api/v1/users.py`:
     - `POST /profile/block/{user_id}` - Block a user
     - `DELETE /profile/block/{user_id}` - Unblock a user
     - `GET /profile/blocked` - List blocked users (use cursor pagination)

7. **Integration Points**
   - Update content access checks to respect blocking (posts, comments, messages)
   - Update search to exclude blocked users
   - Update follow functionality to prevent following blocked users

**Acceptance Criteria:**
- Users can block other users
- Blocked users cannot interact with blocker's content
- Blocked users are excluded from search results
- Block list is retrievable

---

### 1.2 Threaded Comments

**Objective:** Enable comment replies and threaded conversations using efficient query patterns

**Steps:**

1. **Database Schema Updates**
   - Add `parent_id` column to `comments` table (nullable, foreign key to comments.id)
   - Add `depth` column to track nesting level
   - Add index on `parent_id` for query performance
   - Add composite index on `(post_id, parent_id)` for thread queries
   - Create Alembic migration script

2. **Model Updates**
   - Update `app/models/comment.py` to add parent relationship and depth field
   - Add self-referential relationship for replies

3. **Schema Updates**
   - Update `app/schemas/comment.py` to include parent_id, depth, and replies in response
   - Add depth limit configuration (max 5-10 levels)

4. **Repository Updates**
   - Update `app/repositories/comment.py` with methods:
     - `get_replies(parent_id, cursor, limit)` - Get comment replies using cursor pagination
     - `get_thread(comment_id, max_depth)` - Get full comment thread using Recursive CTE
     - **CRITICAL:** Use SQLAlchemy Recursive CTE for single-query thread fetching
     - Avoid N+1 queries by fetching entire tree in one database round-trip

5. **Service Updates**
   - Update `app/services/comment_service.py` to handle parent_id in comment creation
   - Add validation: parent comment must exist and belong to same post
   - Add depth validation: prevent nesting beyond max depth

6. **API Endpoint Updates**
   - Add endpoint to `app/api/v1/comments.py`:
     - `GET /{comment_id}/replies` - Get comment replies with cursor pagination
   - Update `POST /` to accept optional `parent_id` in CommentCreate schema

7. **Response Structure**
   - Ensure CommentResponse includes nested replies up to configured depth limit
   - Use Recursive CTE results to build nested structure in application layer

**Acceptance Criteria:**
- Comments can have replies
- Replies can be retrieved with pagination
- Comment threads are properly structured
- Parent comment validation works correctly

---

## Phase 2: WebSocket Enhancement (Weeks 4-5)

### 2.1 Chat Lifecycle Events

**Objective:** Implement structured chat events (join, leave, delete, seen) using Socket.IO with async operations

**Steps:**

1. **Event Schema Definition**
   - Create `app/schemas/websocket_events.py` with event schemas:
     - `ChatEvent` (chat_id, actor_id, message_id, recipient_id, event_type)
     - `NotificationEvent` (notification_id, actor_id, recipient_id, event_type)
     - `EventType` enum (JOIN_CHAT, LEAVE_CHAT, DELETE_MESSAGE, SEEN_MESSAGE, etc.)

2. **WebSocket Manager Enhancement**
   - Update `app/api/websocket/manager.py`:
     - Add method `broadcast_chat_event(chat_id, event, data)` for chat-specific events
     - Add method `send_notification_event(user_id, event, data)` for user-specific events
     - Track chat room membership more precisely

3. **Event Handlers Implementation**
   - Update `app/api/websocket/handlers.py`:
     - Add handler for `leave_chat` event
     - Add handler for `delete_message` event
     - Add handler for `seen_message` event
     - Enhance `join_chat` to broadcast join event to chat members
   - **CRITICAL:** All handlers must use async database operations (AsyncSession)
   - **CRITICAL:** No blocking calls in WebSocket event handlers

4. **Service Integration**
   - Update `app/services/chat_service.py` to trigger WebSocket events on:
     - Member addition (broadcast JOIN_CHAT)
     - Member removal (broadcast LEAVE_CHAT)
   - Update `app/services/message_service.py` to trigger events on:
     - Message deletion (broadcast DELETE_MESSAGE)
     - Message seen status update (broadcast SEEN_MESSAGE)
   - **Use Unit of Work pattern** for operations that modify database and send events

5. **Client Event Documentation**
   - Document expected event format for frontend integration
   - Document event naming convention

6. **Testing**
   - Test event broadcasting to chat rooms
   - Test event delivery to specific users
   - Test event payload structure
   - **Test async performance** under load

**Acceptance Criteria:**
- Chat members receive join/leave events
- Message deletion is broadcast to chat participants
- Message seen status is communicated to sender
- Events are properly structured and typed

---

### 2.2 Real-time Notifications

**Objective:** Deliver notifications in real-time via WebSocket with async operations

**Steps:**

1. **Notification Event Structure**
   - Define notification event schema in `app/schemas/websocket_events.py`
   - Include notification data: type, content, actor, timestamp

2. **WebSocket Manager Updates**
   - Add method `send_notification(user_id, notification_data)` in ConnectionManager
   - Ensure user-specific notification delivery

3. **Service Integration**
   - Update `app/services/notification_service.py`:
     - Add WebSocket notification trigger in `create_notification()`
     - Add WebSocket notification trigger in notification processing methods
   - **CRITICAL:** All notification operations must use async database calls

4. **Notification Types Mapping**
   - Map notification types to WebSocket events:
     - FOLLOW_REQUEST
     - COMMENT
     - REPOST
     - REACTION
     - MENTION (if applicable)

5. **API Endpoint Updates**
   - Update `app/api/v1/notifications.py`:
     - Add WebSocket trigger when marking notifications as read
     - Ensure real-time unread count updates
     - Use cursor pagination for list endpoints

6. **Frontend Integration Points**
   - Document notification event format
   - Document connection requirements for receiving notifications

**Acceptance Criteria:**
- New notifications are delivered via WebSocket
- Notification read status updates are real-time
- Unread count updates are pushed to clients
- All notification types are supported

---

## Phase 3: Content Features (Weeks 6-7)

### 3.1 Poll Voting System

**Objective:** Enable poll post voting functionality with async operations

**Steps:**

1. **Database Schema Updates**
   - Add `polls` table with columns: `id`, `post_id`, `question`, `expires_at`
   - Add `poll_options` table with columns: `id`, `poll_id`, `option_text`, `vote_count`
   - Add `poll_votes` table with columns: `id`, `poll_option_id`, `user_id`, `voted_at`
   - Add unique constraint on `(poll_option_id, user_id)`
   - Create Alembic migration scripts

2. **Model Implementation**
   - Create `app/models/poll.py` with Poll, PollOption, PollVote models
   - Define relationships with Post and User models

3. **Repository Implementation**
   - Create `app/repositories/poll.py` with PollRepository
   - Implement methods: `create_poll()`, `add_option()`, `vote()`, `get_results()`
   - **CRITICAL:** All methods must use AsyncSession and async operations

4. **Schema Implementation**
   - Create `app/schemas/poll.py` with:
     - PollCreate, PollResponse schemas
     - PollOptionCreate, PollOptionResponse schemas
     - VoteRequest schema

5. **Service Implementation**
   - Create `app/services/poll_service.py` with PollService
   - Implement voting logic with validation (one vote per user per poll)
   - Implement poll result calculation
   - **Use Unit of Work pattern** for vote operations

6. **API Endpoint**
   - Add endpoint to `app/api/v1/posts.py`:
     - `POST /vote-poll` - Vote on poll option
   - Update PostCreate/PostResponse to include poll data

7. **Post Integration**
   - Update post creation to optionally include poll data
   - Update post queries to include poll results
   - Use cursor pagination for post feeds

**Acceptance Criteria:**
- Posts can include polls
- Users can vote on poll options
- Each user can vote once per poll
- Poll results are calculated correctly
- Poll data is included in post responses

---

### 3.2 Commented Posts Query

**Objective:** Enable retrieval of posts that users have commented on using cursor pagination

**Steps:**

1. **Repository Implementation**
   - Update `app/repositories/post.py` with method:
     - `get_commented_posts(user_id, cursor, limit)` - Get posts commented by user using cursor pagination
   - **CRITICAL:** Use cursor-based pagination for social feed

2. **Service Implementation**
   - Update `app/services/post_service.py` with method:
     - `get_commented_posts(user_id, cursor, limit)`

3. **API Endpoint**
   - Add endpoint to `app/api/v1/posts.py`:
     - `GET /user/{user_id}/commented` - Get posts commented by user with cursor pagination

4. **Query Optimization**
   - Ensure efficient query with proper indexing on (user_id, created_at)
   - Consider caching for frequently accessed data
   - Add composite index on `comments(user_id, post_id, created_at)`

**Acceptance Criteria:**
- Posts commented by user are retrievable
- Pagination works correctly
- Query performance is acceptable

---

## Phase 4: User Management Enhancements (Weeks 8-9)

### 4.1 Advanced User Search

**Objective:** Enable multi-field user search (username, firstname, lastname) using offset pagination

**Steps:**

1. **Database Schema Updates**
   - Ensure `firstname` and `lastname` columns exist in users table
   - Add indexes on firstname and lastname for search performance
   - Create Alembic migration if needed

2. **Model Updates**
   - Update `app/models/user.py` to include firstname and lastname fields

3. **Schema Updates**
   - Update `app/schemas/user.py` to include firstname and lastname in UserCreate and UserResponse

4. **Repository Implementation**
   - Update `app/repositories/user.py` with method:
     - `advanced_search(username, firstname, lastname, page, size)` - Multi-field search
   - **Use offset pagination** (static data, not social feed)

5. **Service Implementation**
   - Update `app/services/user_service.py` with advanced search method

6. **API Endpoint**
   - Add endpoint to `app/api/v1/users.py`:
     - `GET /query` - Advanced user search with optional parameters (username, firstname, lastname, page, size)

7. **Search Logic**
   - Implement partial matching for all fields
   - Combine search conditions with OR logic
   - Apply offset pagination (appropriate for user search)

**Acceptance Criteria:**
- Users can search by username, firstname, lastname
- Search supports partial matching
- Multiple fields can be combined
- Pagination works correctly

---

### 4.2 Follower Management

**Objective:** Add distinct "remove follower" functionality (different from unfollow)

**Steps:**

1. **Repository Implementation**
   - Update `app/repositories/user.py` with method:
     - `remove_follower(user_id, follower_id)` - Remove a specific follower
   - **CRITICAL:** Use async operations

2. **Service Implementation**
   - Update `app/services/user_service.py` with remove_follower method
   - Add validation: can only remove your own followers
   - **Use Unit of Work pattern** for multi-step operations

3. **API Endpoint**
   - Add endpoint to `app/api/v1/users.py`:
     - `POST /followers/remove/{user_id}` - Remove a follower

4. **Notification Integration**
   - Trigger notification when follower is removed (optional)

**Acceptance Criteria:**
- Users can remove specific followers
- Validation prevents removing non-followers
- Distinct from unfollow functionality

---

### 4.3 Activation Code Resend

**Objective:** Enable resending of account activation emails with Redis-backed rate limiting

**Steps:**

1. **Rate Limiting Middleware**
   - Install and configure `slowapi` for rate limiting
   - Add Redis-backed rate limiter for auth endpoints
   - Configure rate limits: 3 requests per 5 minutes per email

2. **Service Implementation**
   - Update `app/services/auth_service.py` with method:
     - `resend_activation_email(email)` - Generate new code and send email
   - **CRITICAL:** Use async operations

3. **Validation**
   - Check if user exists
   - Check if account is already activated
   - **Rate limiting is handled by middleware, not service layer**

4. **API Endpoint**
   - Add endpoint to `app/api/v1/auth.py`:
     - `POST /resend-code` - Resend activation code with email parameter
   - Apply rate limiting middleware to this endpoint

5. **Email Service Integration**
   - Use existing EmailService for sending emails

**Acceptance Criteria:**
- Activation codes can be resent
- Rate limiting prevents abuse
- Validation checks user status

---

## Phase 5: Message Management (Week 10)

### 5.1 Message Deletion

**Objective:** Enable message deletion functionality using soft deletes

**Steps:**

1. **Database Schema Updates**
   - Add `deleted_at` column to messages table (nullable timestamp)
   - Add index on `deleted_at` for filtering
   - Create Alembic migration script

2. **Model Updates**
   - Update `app/models/message.py` to include deleted_at field
   - Add property `is_deleted` for convenience

3. **Repository Implementation**
   - Update `app/repositories/message.py` with method:
     - `soft_delete(message_id)` - Set deleted_at timestamp
     - Update queries to filter out deleted messages by default
   - **CRITICAL:** Use async operations

4. **Service Implementation**
   - Update `app/services/message_service.py` with method:
     - `delete_message(message_id, user_id)` - Soft delete with authorization check
   - **Use Unit of Work pattern** for transaction management

5. **WebSocket Integration**
   - Trigger DELETE_MESSAGE event when message is soft deleted
   - Broadcast to chat participants

6. **API Endpoint**
   - Add endpoint to `app/api/v1/messages.py`:
     - `DELETE /{message_id}` - Soft delete message by ID

7. **Authorization**
   - Only message sender or admin can delete
   - Add authorization checks

8. **Query Updates**
   - Update all message queries to exclude soft-deleted messages
   - Add optional parameter to include deleted messages for admin

**Acceptance Criteria:**
- Messages can be deleted by sender
- Deletion is broadcast via WebSocket
- Authorization checks work correctly

---

## Phase 6: Enhancement Features (Weeks 11-12)

### 6.1 Online Users Query

**Objective:** Enable querying of online users via WebSocket with async operations

**Steps:**

1. **WebSocket Manager Enhancement**
   - Update `app/api/websocket/manager.py`:
     - Add method `get_online_users()` - Return list of online user IDs
     - Track online status more precisely using Redis

2. **Event Handler Implementation**
   - Add handler in `app/api/websocket/handlers.py`:
     - `get_online_users` event - Return online users to requester
   - **CRITICAL:** Use async operations

3. **Service Integration**
   - Update `app/services/user_service.py` to track online status
   - Use Redis for online status tracking (scalable)
   - Integrate with existing online status management

4. **API Endpoint**
   - Add REST endpoint for online users (optional):
     - `GET /users/online` - Get online users

**Acceptance Criteria:**
- Online users can be queried via WebSocket
- Online status is accurately tracked
- Response includes user details

---

### 6.2 Public Chat Channel

**Objective:** Add public broadcasting capability for community features

**Steps:**

1. **WebSocket Manager Enhancement**
   - Update `app/api/websocket/manager.py`:
     - Add method `broadcast_to_public(event, data)` - Broadcast to all connected users
     - Create public room concept using Redis pub/sub

2. **Event Handler Implementation**
   - Add handler in `app/api/websocket/handlers.py`:
     - `public_message` event - Broadcast to public channel
   - **CRITICAL:** Use async operations

3. **Use Cases**
   - System announcements
   - Community updates
   - Public chat rooms

**Acceptance Criteria:**
- Public broadcasting works
- All connected users receive public messages
- Channel is properly managed

---

### 6.3 Rate Limiting Implementation

**Objective:** Implement comprehensive rate limiting using Redis-backed middleware

**Steps:**

1. **Middleware Setup**
   - Install `slowapi` for rate limiting
   - Configure Redis backend for rate limit storage
   - Create rate limiting configuration

2. **Rate Limit Rules**
   - Define rate limits for different endpoint types:
     - Auth endpoints: 5 requests per minute
     - Content creation: 10 requests per minute
     - API endpoints: 100 requests per minute
     - WebSocket: Connection limits

3. **Endpoint Application**
   - Apply rate limiting to all auth endpoints
   - Apply rate limiting to content creation endpoints
   - Apply rate limiting to sensitive operations

4. **Monitoring**
   - Log rate limit violations
   - Add metrics for rate limit hits
   - Configure alerts for abuse patterns

**Acceptance Criteria:**
- Rate limiting is enforced across all sensitive endpoints
- Redis-backed storage is scalable
- Rate limit violations are logged and monitored

---

## Phase 7: API Consistency (Week 13)

### 7.1 Partial Update Endpoints (PATCH)

**Objective:** Add PATCH endpoints for partial resource updates

**Steps:**

1. **Identify Resources**
   - Posts, Comments, Users, Chats

2. **Schema Updates**
   - Ensure all schemas support partial updates (exclude_unset=True)

3. **API Endpoint Additions**
   - Add PATCH endpoints to:
     - `app/api/v1/posts.py` - `PATCH /{post_id}`
     - `app/api/v1/comments.py` - `PATCH /{comment_id}`
     - `app/api/v1/users.py` - `PATCH /` (already exists, verify)
     - `app/api/v1/chats.py` - `PATCH /{chat_id}`

4. **Service Updates**
   - Update services to handle partial updates correctly
   - **CRITICAL:** Use async operations

**Acceptance Criteria:**
- PATCH endpoints work for all major resources
- Partial updates only modify provided fields
- Validation works correctly

---

### 7.2 Pagination Standardization

**Objective:** Standardize pagination across all endpoints using appropriate pagination type

**Steps:**

1. **Pagination Classification**
   - **Cursor-Based Pagination** (social feeds):
     - Posts feed
     - Notifications
     - Chat messages
     - Commented posts
     - User's posts
   - **Offset-Based Pagination** (static/admin):
     - User search
     - User followers/following
     - Admin user lists
     - Blocked users list

2. **Schema Updates**
   - `app/schemas/pagination.py` already created in Phase 0.2
   - Ensure all endpoints use appropriate pagination schema

3. **Endpoint Updates**
   - Apply cursor pagination to social feeds:
     - `GET /posts` - Use cursor pagination
     - `GET /posts/saved` - Use cursor pagination
     - `GET /notifications` - Use cursor pagination
     - `GET /messages/chat/{chat_id}` - Use cursor pagination
     - `GET /user/{user_id}/commented` - Use cursor pagination
   - Apply offset pagination to static data:
     - `GET /users/search` - Use offset pagination
     - `GET /users/{user_id}/followers` - Use offset pagination
     - `GET /users/{user_id}/following` - Use offset pagination
     - `GET /users/profile/blocked` - Use offset pagination

4. **Repository Updates**
   - Update repository methods to return appropriate pagination results
   - Implement cursor-based queries for social feeds
   - Implement count queries for offset pagination

**Acceptance Criteria:**
- All list endpoints use consistent pagination
- Pagination parameters are standardized
- Response structure includes metadata

---

## Phase 8: Testing & Documentation (Weeks 14-15)

### 8.1 Testing

**Steps:**

1. **Unit Tests**
   - Write unit tests for all new services
   - Write unit tests for all new repositories
   - Write unit tests for WebSocket handlers
   - **Test async operations** thoroughly

2. **Integration Tests**
   - Write integration tests for new API endpoints
   - Write integration tests for WebSocket events
   - Test database migrations
   - **Test Unit of Work pattern** rollback scenarios

3. **WebSocket Testing**
   - Test Socket.IO connection lifecycle
   - Test event broadcasting
   - Test authentication over WebSocket
   - **Test async performance** under load
   - Test Redis pub/sub functionality

4. **Performance Testing**
   - Test query performance with large datasets
   - Test cursor pagination performance vs offset
   - Test WebSocket performance with multiple connections
   - Test Recursive CTE performance for comment threads
   - Optimize slow queries

5. **Rate Limiting Tests**
   - Test rate limiting enforcement
   - Test Redis-backed rate limit storage
   - Test rate limit bypass attempts

**Acceptance Criteria:**
- All new features have test coverage
- Tests pass consistently
- Performance meets requirements

---

### 8.2 Documentation

**Steps:**

1. **API Documentation**
   - Update OpenAPI/Swagger documentation for all new endpoints
   - Document request/response schemas
   - Document authentication requirements
   - Document pagination types (cursor vs offset)

2. **WebSocket Documentation**
   - Document Socket.IO event names and formats
   - Document connection process
   - Document event flow for each feature
   - Document async operation requirements

3. **Migration Guide**
   - Document database schema changes
   - Provide migration scripts
   - Document data migration if needed
   - Document Unit of Work pattern usage

4. **Frontend Integration Guide**
   - Document new endpoints for frontend team
   - Provide example requests/responses
   - Document WebSocket event handling
   - Document cursor pagination implementation
   - Document rate limiting behavior

5. **Architecture Documentation**
   - Document async/await requirements
   - Document Unit of Work pattern
   - Document cursor vs offset pagination decision matrix
   - Document Redis usage patterns

**Acceptance Criteria:**
- All new features are documented
- Documentation is accurate and complete
- Frontend team can integrate without questions

---

## Phase 9: Deployment & Monitoring (Week 16)

### 9.1 Deployment Preparation

**Steps:**

1. **Environment Configuration**
   - Update environment variables for new features
   - Configure Redis for WebSocket scaling and rate limiting
   - Update Docker configuration if needed
   - Verify asyncpg driver configuration

2. **Database Migration**
   - Test migrations on staging environment
   - Prepare rollback plan
   - Schedule migration window
   - **Test Recursive CTE queries** on production-like data

3. **Feature Flags**
   - Consider feature flags for gradual rollout
   - Configure monitoring for new features
   - Configure Redis connection pooling

4. **Capacity Planning**
   - Plan Redis capacity for rate limiting and WebSocket scaling
   - Plan database capacity for soft-deleted data retention
   - Plan for cursor pagination storage

**Acceptance Criteria:**
- Deployment process is documented
- Rollback plan is tested
- Monitoring is in place

---

### 9.2 Monitoring & Observability

**Steps:**

1. **Logging**
   - Add logging for new features
   - Log WebSocket events
   - Log critical operations
   - **Log async operation performance**
   - Log rate limit violations

2. **Metrics**
   - Add metrics for API endpoints
   - Add metrics for WebSocket connections
   - Add metrics for feature usage
   - Add metrics for cursor pagination performance
   - Add metrics for Redis operations
   - Add metrics for rate limit hits

3. **Alerting**
   - Set up alerts for errors
   - Set up alerts for performance issues
   - Set up alerts for WebSocket connection issues
   - Set up alerts for Redis connection issues
   - Set up alerts for async operation blocking
   - Set up alerts for rate limit abuse

**Acceptance Criteria:**
- All new features are monitored
- Alerts are configured
- Issues are detectable

---

## Summary Timeline

| Phase | Duration | Focus Area |
|-------|----------|------------|
| Phase 0 | Week 1 | Foundation & Architecture (Events, Redis, Unit of Work, Cursor Pagination) |
| Phase 1 | Weeks 2-3 | Critical Social Features (Blocking, Threaded Comments with CTE) |
| Phase 2 | Weeks 4-5 | WebSocket Enhancement (Chat Events, Real-time Notifications with async) |
| Phase 3 | Weeks 6-7 | Content Features (Polls, Commented Posts with cursor pagination) |
| Phase 4 | Weeks 8-9 | User Management (Advanced Search, Follower Management, Activation Resend with rate limiting) |
| Phase 5 | Week 10 | Message Management (Soft Deletion) |
| Phase 6 | Weeks 11-12 | Enhancement Features (Online Users, Public Chat, Rate Limiting) |
| Phase 7 | Week 13 | API Consistency (PATCH, Pagination standardization) |
| Phase 8 | Weeks 14-15 | Testing & Documentation |
| Phase 9 | Week 16 | Deployment & Monitoring |

**Total Duration:** 16 weeks

---

## Success Criteria

The implementation will be considered complete when:

1. All missing features from the functional parity analysis are implemented
2. WebSocket functionality matches or exceeds Spring Boot capabilities (using Socket.IO)
3. All new features have comprehensive test coverage
4. API documentation is complete and accurate
5. Performance meets or exceeds legacy system benchmarks
6. Frontend team can successfully integrate all new features
7. **All WebSocket handlers use async database operations**
8. **Social feeds use cursor-based pagination**
9. **Unit of Work pattern is used for multi-step operations**
10. **Messages use soft deletion**
11. **Rate limiting is implemented via middleware**
12. **Comment threads use Recursive CTE for efficient queries**

---

## Risk Mitigation

**Technical Risks:**
- WebSocket scaling: Use Redis-backed Socket.IO for horizontal scaling (Phase 0)
- Database performance: Add proper indexes, use cursor pagination for social feeds
- Async operation blocking: Strict enforcement of async/await in WebSocket handlers
- Recursive CTE performance: Test with large comment threads, add depth limits
- Redis capacity: Plan for rate limiting and pub/sub storage
- Soft deletion data growth: Implement cleanup policies for deleted data

**Timeline Risks:**
- Scope creep: Stick to defined phases, defer non-critical features
- Resource constraints: Prioritize critical features, adjust timeline if needed
- Foundation phase delay: Phase 0 is critical for subsequent phases

**Integration Risks:**
- Frontend integration: Provide clear documentation for cursor pagination
- Data migration: Test thoroughly, have rollback plan ready
- Pagination migration: Document transition from offset to cursor for frontend

**Architecture Risks:**
- Unit of Work pattern adoption: Ensure team training and documentation
- Cursor pagination complexity: Provide clear examples and guidelines
- Async debugging: Add logging and monitoring for async operations

---

**Document Version:** 2.0  
**Last Updated:** July 6, 2026  
**Based on:** FUNCTIONAL_PARITY_ANALYSIS.md v1.0  
**Changes:** Added architectural improvements (async operations, cursor pagination, Unit of Work, soft deletes, rate limiting, Recursive CTE)
