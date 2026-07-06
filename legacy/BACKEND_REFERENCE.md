# QuietSpace Backend Reference Guide

## Table of Contents
1. [Overview](#overview)
2. [API Reference](#api-reference)
3. [Architecture & Layers](#architecture-and-layers)
4. [Containerization & Deployment](#containerization-and-deployment)
5. [Consumption Patterns](#consumption-patterns)
6. [Error Handling](#error-handling)
7. [Security](#security)

---

## Overview
QuietSpace is a privacy-focused social media backend providing features like:
- Secure Authentication & Authorization (JWT-based)
- User Profiles & Follow System
- Posts, Polls, Comments & Reactions
- Real-time Chat & Notifications via WebSockets
- Photo upload & media management
- Email verification & notifications

This document is **platform-agnostic** and intended for both backend and frontend developers, regardless of their tech stack.

---

## API Reference

### Base Path & Versioning
All API endpoints are prefixed with `/api/v1` - future versions will increment this prefix.

### Authentication Requirements
- Public endpoints: `/api/v1/auth/**`, `/api/v1/photos/{name}`, `/ws/**`, Swagger UI paths
- Protected endpoints: Require a valid JWT access token in the `Authorization` header: `Bearer <token>`
- Admin endpoints: Require valid JWT with `ADMIN` role

### Pagination
Endpoints returning lists use pagination. Query params:
- `page-number`: 0-based page index (default: 0)
- `page-size`: max items per page (default: varies, typically 20-50)

---

### Authentication APIs (`/api/v1/auth`)
| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---------------|
| `/register` | POST | Register new user & send activation email | No |
| `/authenticate` | POST | Login & get JWT tokens | No |
| `/activate-account` | POST | Activate account using token | No |
| `/signout` | POST | Sign out (invalidates access token) | Yes |
| `/refresh-token` | POST | Get new access token using refresh token | Yes (via refresh token) |
| `/resend-code` | POST | Resend account activation email | No |

---

### User APIs (`/api/v1/users`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/search` | GET | Search users by username (with pagination) |
| `/query` | GET | Query users by username, firstname, lastname (with pagination) |
| `/{userId}` | GET | Get user by ID |
| `/{userId}` | DELETE | Delete user (admin or self) |
| `/profile` | GET | Get current logged-in user profile |
| `/profile` | PATCH | Update current user profile |
| `/profile/block/{userId}` | POST | Block a user |
| `/profile/settings` | PATCH | Update profile settings |
| `/follow/{userId}/toggle-follow` | POST | Toggle follow/unfollow a user |
| `/followers/remove/{userId}` | POST | Remove a follower |
| `/{userId}/followings` | GET | List users a user follows (with pagination) |
| `/{userId}/followers` | GET | List a user's followers (with pagination) |

---

### Post APIs (`/api/v1/posts`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Get all posts (paginated) |
| `/search` | GET | Search posts by query string |
| `/user/{userId}` | GET | Get posts by user (paginated) |
| `/user/{userId}/commented` | GET | Get posts a user commented on |
| `/saved` | GET | Get current user's saved posts |
| `/saved/{postId}` | PATCH | Save/unsave a post |
| `/` | POST | Create a new post (supports photo upload) |
| `/repost` | POST | Repost an existing post |
| `/{postId}` | GET | Get post by ID |
| `/{postId}` | PUT | Update post by ID |
| `/{postId}` | PATCH | Partially update post |
| `/{postId}` | DELETE | Delete post |
| `/vote-poll` | POST | Vote on a poll |

---

### Comment APIs (`/api/v1/comments`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/post/{postId}` | GET | Get comments on a post (paginated) |
| `/user/{userId}` | GET | Get comments by a user (paginated) |
| `/user/{userId}/post/{postId}/latest` | GET | Get user's latest comment on a post |
| `/{commentId}/replies` | GET | Get replies to a comment (paginated) |
| `/{commentId}` | GET | Get comment by ID |
| `/` | POST | Create a comment |
| `/{commentId}` | PUT | Update comment |
| `/{commentId}` | PATCH | Partially update comment |
| `/{commentId}` | DELETE | Delete comment |

---

### Reaction APIs (`/api/v1/reactions`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/user` | GET | Get reactions by a user for specific content type |
| `/content` | GET | Get reactions for specific content |
| `/toggle-reaction` | POST | Add/remove a reaction |
| `/count` | GET | Count reactions of a specific type for content |

---

### Chat APIs (`/api/v1/chats`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/{chatId}` | GET | Get chat by ID |
| `/members/{userId}` | GET | Get all chats a user is part of |
| `/` | POST | Create a new chat |
| `/{chatId}/members/add/{userId}` | PATCH | Add user to chat |
| `/{chatId}/members/remove/{userId}` | PATCH | Remove user from chat |
| `/{chatId}` | DELETE | Delete a chat |

---

### Message APIs (`/api/v1/messages`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | POST | Create a message |
| `/{messageId}` | DELETE | Delete a message |
| `/chat/{chatId}` | GET | Get messages for a chat (paginated) |
| `/chat/{chatId}/message/{messageId}` | GET | Get specific message in a chat |

---

### Notification APIs (`/api/v1/notifications`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Get all notifications (paginated) |
| `/type/{notificationType}` | GET | Get notifications by type |
| `/count-pending` | GET | Get count of unread notifications |
| `/seen/{contentId}` | POST | Mark notifications as seen for content |
| `/process` | POST | Process a notification (internal use) |
| `/process-reaction` | POST | Process reaction notification (internal use) |

---

### Photo APIs (`/api/v1/photos`)
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/profile` | POST | Upload a profile photo |
| `/{name}` | GET | Get a photo by its name |
| `/profile/{userId}` | DELETE | Delete a user's profile photo |

---

### Admin APIs (`/api/v1/admin`)
| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---------------|
| `/` | GET | Admin hello endpoint | Yes (Admin role) |
| `/{userId}` | DELETE | Delete a user by ID | Yes (Admin role) |
| `/users` | GET | Get paginated list of all users | Yes (Admin role) |

---

### WebSocket APIs (STOMP over WebSocket)
- **WebSocket Endpoint**: `/ws`
- **Broker Prefixes**: `/user` (user-specific), `/public` (broadcast), `/private` (private messaging)
- **Application Prefix**: `/app`

#### Key WebSocket Destinations
| Destination | Type | Purpose |
|-------------|------|---------|
| `/app/user/setOnlineStatus` | Send | Set user online/offline status |
| `/app/user/onlineUsers` | Send | Request list of online users |
| `/user/onlineUsers` | Subscribe | Receive list of online users |
| `/app/public/chat` | Send/Subscribe | Public chat messages |
| `/app/private/chat` | Send | Send private chat message |
| `/user/private/chat` | Subscribe | Receive private messages |
| `/app/private/chat/delete/{messageId}` | Send | Delete a chat message |
| `/app/private/chat/seen/{messageId}` | Send | Mark message as seen |
| `/user/private/chat/event` | Subscribe | Receive chat events (message deleted, seen, etc.) |
| `/app/private/chat/leave` | Send | Leave a chat |
| `/app/private/chat/join` | Send | Join a chat |
| `/app/private/notifications/seen/{notificationId}` | Send | Mark notification as seen |
| `/user/private/notifications` | Subscribe | Receive real-time notifications |

---

## Architecture and Layers

### Layer Overview
QuietSpace backend follows a clean, layered architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                        │
│                      (REST Controllers, WebSocket)               │
├─────────────────────────────────────────────────────────────────┤
│                         Service Layer                            │
│                     (Business Logic, Services)                  │
├─────────────────────────────────────────────────────────────────┤
│                        Data Access Layer                         │
│              (Repositories, JPA/Hibernate, Database)            │
└─────────────────────────────────────────────────────────────────┘
```

#### 1. Presentation Layer
- **Controllers**: Handle HTTP requests, validate inputs, delegate to services, return responses
- **WebSocket Handlers**: Manage real-time bi-directional communication
- **DTOs (Request/Response Models)**: Define data structures for API input/output
- **Global Exception Handling**: Centralized error management

#### 2. Service Layer
- Implements all business logic
- Handles transaction management
- Orchestrates data from repositories
- Contains service interfaces and implementations

#### 3. Data Access Layer
- Repositories abstract database interactions
- Entities represent database tables
- Supports complex queries, pagination, and specifications
- Uses JPA/Hibernate for ORM

### Core Modules

| Module | Responsibilities |
|--------|-------------------|
| **Auth** | Registration, login, JWT token generation/validation, account activation |
| **User** | User profiles, following/blocking, settings, online status |
| **Post** | Post creation/updates/deletion, polls, saved posts |
| **Comment** | Comment management, replies |
| **Reaction** | Reaction (like/love/etc.) management |
| **Chat** | Chat rooms, member management |
| **Message** | Message sending/receiving, read receipts |
| **Notification** | Notification generation/delivery/marking as seen |
| **Photo** | Photo upload, compression, retrieval |

---

## Containerization and Deployment

### Docker
QuietSpace uses multi-stage Docker builds for optimized images.

**Dockerfile Location**: `/infrastructure/docker/Dockerfile`
- Build stage uses Maven + JDK 17 to compile app
- Extractor stage unpacks Spring Boot jar layers for efficient caching
- Final stage uses JRE 17 Alpine to run app

**Key Services (docker-compose.yaml)**:
| Service | Image | Purpose |
|---------|-------|---------|
| `quietspace-monolith-db` | `mysql:8.0` | MySQL database |
| `quietspace-monolith` | Custom build | Main backend application |
| `quietspace-frontend` | `thural/quietspace:frontend` | Frontend app |
| `mail-dev` | `maildev/maildev` | Local email testing server |

### Kubernetes Deployment
Located in `/infrastructure/k8s/`

**Components**:
1. **ConfigMap (`common-config.yaml`)**: Non-sensitive environment variables
2. **Secret (`common-secret.yaml`)**: Sensitive credentials (base64 encoded)
3. **MySQL Database**:
   - Deployment: `mysql-deployment`
   - Service: `mysql-service` (ClusterIP)
   - PersistentVolume + PersistentVolumeClaim for data persistence
4. **Backend**:
   - Deployment: `backend-deployment` (3 replicas)
   - Service: `backend-service` (NodePort on 31000)
5. **Frontend**:
   - Deployment: `frontend-deployment` (3 replicas)
   - Service: `frontend-service` (LoadBalancer/NodePort on 30000)
6. **Ingress**: Routes external traffic to services

**Environment Variables**:
| Variable | Purpose |
|----------|---------|
| `JWT_SECRET_KEY` | Secret for signing JWT tokens |
| `DB_URL` | JDBC URL for MySQL database |
| `DB_USER_USERNAME` | Database username |
| `DB_USER_PASSWORD` | Database password |
| `FRONTEND_URL` | Frontend URL for CORS and email links |
| `MAILDEV_HOST` / `MAILDEV_PORT` | Email server connection |
| `ACTIVATION_URL` | URL for account activation link in emails |

---

## Consumption Patterns

### Authentication Flow
1. **Register**: User sends `POST /api/v1/auth/register` → receives account activation email
2. **Activate**: User clicks link in email → `POST /api/v1/auth/activate-account?token=<token>`
3. **Login**: User sends `POST /api/v1/auth/authenticate` with credentials → receives `accessToken` and `refreshToken`
4. **Authorize Requests**: Attach `accessToken` to all protected API requests via `Authorization: Bearer <accessToken>` header
5. **Refresh Token**: When `accessToken` expires, send `POST /api/v1/auth/refresh-token` with `refreshToken` to get new `accessToken`
6. **Logout**: Send `POST /api/v1/auth/signout` to invalidate tokens

### WebSocket Connection Flow
1. Establish WebSocket connection to `/ws`
2. Authenticate using JWT token via STOMP CONNECT frame (pass in `Authorization` header)
3. Subscribe to relevant destinations (e.g., `/user/private/chat`, `/user/private/notifications`)
4. Send messages to application destinations (prefixed by `/app`)

### Common Integration Pitfalls
- **Pagination**: Always handle paginated responses in frontend (don't assume single page)
- **JWT Expiry**: Implement token refresh flow before access token expires
- **WebSocket Reconnection**: Handle WebSocket disconnects and implement reconnection logic
- **CORS**: Ensure frontend URL is properly configured in backend CORS settings
- **File Uploads**: Use multipart/form-data for photo uploads

---

## Error Handling
- **Global Exception Handler**: Centralized error management returns consistent error responses
- **HTTP Status Codes**:
  - `200 OK`: Success
  - `201 Created`: Resource created
  - `204 No Content`: Success with no response body
  - `400 Bad Request`: Invalid request data
  - `401 Unauthorized`: Authentication required/failed
  - `403 Forbidden`: Insufficient permissions
  - `404 Not Found`: Resource doesn't exist
  - `500 Internal Server Error`: Server-side error

---

## Security
- **Stateless Authentication**: JWT-based
- **Password Hashing**: BCrypt
- **Role-Based Authorization**: `USER` and `ADMIN` roles with fine-grained permissions
- **CORS Configuration**: Restricts allowed origins
- **CSRF Protection**: Disabled (stateless JWT-based auth)
- **SQL Injection Prevention**: JPA/Hibernate parameterized queries
