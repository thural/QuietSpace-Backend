# Unit Test Implementation Plan — quietspace-backend

> **Target:** Reach ~70% instruction coverage from unit tests per the testing pyramid standard.  
> **Framework:** JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) — no Spring context in unit tests.  
> **Pattern:** AAA (Arrange-Act-Assert), behavior-based assertions, expressive test names.  

---

## Testing Philosophy: Behavior Over Implementation

Every test in this plan is designed around one rule:

> **Test what the caller observes — not how the method achieves it.**

| Do (Behavior) | Don't (Implementation) |
|---|---|
| Assert return values and exceptions | Verify private method calls or internal setter invocations |
| Use `ArgumentCaptor` to assert WHAT data was persisted/sent | Verify that specific setters were called on mock entities |
| `verify(repository.save(...))` — persistence IS the contract | `verify(entity.setEnabled(true))` — internal object mutation is an implementation detail |
| Verify side effects that external callers depend on (email sent, WebSocket event pushed) | Verify intermediate utility calls or helper delegations |
| Test observable error paths (not-found, unauthorized, validation) | Test internal conditional branches that produce the same observable outcome |

When these tests pass after a refactor, the code still works — that's the safety net.

---

## Priority Tiers

| Tier | Classes | Effort Est. | Impact |
|---|---|---|---|
| **Tier 1** | `AuthService`, `AuthController` | 3–4 days | Core auth flow — 0% → ~75% |
| **Tier 2** | `PhotoServiceImpl`, `NotificationServiceImpl`, `PhotoController`, `NotificationController` | 3–4 days | Media + notifications |
| **Tier 3** | `AdminController`, `ReactionController`, `EmailService`, `CommonServiceImpl` | 1–2 days | Admin, reaction, email |
| **Tier 4** | `UserServiceImpl`, `PostServiceImpl`, `MessageServiceImpl`, `ChatServiceImpl`, `CommentServiceImpl` (gap-filling) | 3–4 days | Complete partial coverage |
| **Tier 5** | `JwtService`, `JwtFilter`, `PagingProvider`, `PageUtils`, `ImageCompressionUtil` | 2–3 days | Security + utilities |
| **Total** | **~20 new test classes** | **~14 days** | **40% → ~70%** |

---

## Tier 1 — Critical (Core Authentication)

### 1.1 `AuthServiceTest`

**File:** `src/test/java/dev/thural/quietspace/authentication/service/AuthServiceTest.java`

**Dependencies to mock:** `UserRepository`, `UserService`, `PasswordEncoder`, `JwtService`, `AuthenticationManager`, `EmailService`, `TokenRepository`

**Behavior-based test scenarios:**

| # | Test | Arrange | Act | Assert |
|---|---|---|---|---|
| 1 | `register_givenValidRequest_shouldSaveUserAndSendEmail` | Mock `passwordEncoder.encode()` → `"encoded"`; `userRepository.save(any())` returns the input via `Answer` | `authService.register(validRequest)` | `ArgumentCaptor<User>` — captured user has `role=USER`, `enabled=false`, `accountLocked=false`, `profileSettings` not null; verify `emailService.sendEmail()` called once |
| 2 | `register_whenEmailServiceFails_shouldPropagate` | `emailService.sendEmail()` throws `MessagingException` | `authService.register(validRequest)` | Assert `MessagingException` propagates |
| 3 | `authenticate_givenValidCredentials_shouldReturnAuthResponseWithTokens` | `authenticationManager.authenticate()` returns mock `Authentication` with mock `User` principal; `jwtService.generateToken()` → `"access-token"`; `generateRefreshToken()` → `"refresh-token"` | `authService.authenticate(validRequest)` | `AuthResponse.accessToken` is `"access-token"`; `.refreshToken` is `"refresh-token"`; `.userId` is the user's UUID string; `.message` contains "successful" |
| 4 | `authenticate_givenBadCredentials_shouldThrow` | `authenticationManager.authenticate()` throws `BadCredentialsException` | `authService.authenticate(anyRequest)` | Assert `BadCredentialsException` propagates (or whatever the manager throws) |
| 5 | `activateAccount_givenValidToken_shouldNotThrow` | `tokenRepository.findByToken("valid-code")` → token with future `expireDate`; `userRepository.findById(...)` → user | `authService.activateAccount("valid-code")` | `assertDoesNotThrowAnyException` |
| 6 | `activateAccount_givenExpiredToken_shouldResendEmailAndThrow` | Token with past `expireDate` | `authService.activateAccount("expired-code")` | Assert `RuntimeException` (or specific) thrown with message containing "expired"; verify `emailService.sendEmail()` called (new token sent) |
| 7 | `activateAccount_givenNonExistentToken_shouldThrowActivationTokenException` | `tokenRepository.findByToken(any())` → empty | `authService.activateAccount("bad-code")` | Assert `ActivationTokenException` thrown |
| 8 | `signout_givenValidHeader_shouldBlacklistAndClearContext` | `authHeader = "Bearer some.jwt.token"`; `tokenRepository.existsByToken()` → false; `userRepository.findUserByUsername()` → user | `authService.signout(authHeader)` | Verify `tokenRepository.save(any(Token.class))` called once (blacklist persisted); `SecurityContextHolder.getContext().getAuthentication()` is null |
| 9 | `signout_givenNullHeader_shouldDoNothing` | `authHeader = null` | `authService.signout(null)` | Verify `tokenRepository.save()` never called |
| 10 | `signout_givenNonBearerHeader_shouldDoNothing` | `authHeader = "Basic ..."` | `authService.signout(authHeader)` | Same as above |
| 11 | `refreshToken_givenValidToken_shouldReturnNewAccessToken` | `authHeader = "Bearer valid.refresh.token"`; `tokenRepository.existsByToken()` → false; `jwtService.extractUsername()` → `"user"`; `jwtService.isTokenValid()` → true; `jwtService.generateToken()` → `"new-access-token"` | `authService.refreshToken(authHeader)` | `AuthResponse.accessToken` is `"new-access-token"`; `.message` contains "refreshed" |
| 12 | `refreshToken_givenExpiredOrBlacklistedToken_shouldReturnFailure` | `tokenRepository.existsByToken()` → true (blacklisted) | `authService.refreshToken(authHeader)` | `AuthResponse.message` contains "failed"; `accessToken` is null |
| 13 | `refreshToken_givenMissingHeader_shouldReturnFailure` | `authHeader = null` | `authService.refreshToken(null)` | `AuthResponse.message` contains "failed" |
| 14 | `addToBlacklist_givenNewToken_shouldSave` | `tokenRepository.existsByToken()` → false; `userRepository.findUserByUsername()` → user | `authService.addToBlacklist("Bearer some.jwt", "user")` | Verify `tokenRepository.save(any(Token.class))` called once |
| 15 | `addToBlacklist_givenAlreadyBlacklisted_shouldNotSave` | `tokenRepository.existsByToken()` → true | `authService.addToBlacklist("Bearer some.jwt", "user")` | Verify `tokenRepository.save()` never called |
| 16 | `resendActivationToken_givenEnabledAccount_shouldThrow` | `userRepository.findUserEntityByEmail()` → user where `isEnabled()=true` | `authService.resendActivationToken("email")` | Assert `ActivationTokenException` with "already been activated" |
| 17 | `resendActivationToken_givenDisabledAccount_shouldResendEmail` | `findUserEntityByEmail()` → user where `isEnabled()=false` | `authService.resendActivationToken("email")` | Verify `emailService.sendEmail()` called once |
| 18 | `resendActivationToken_givenUnknownEmail_shouldThrow` | `findUserEntityByEmail()` → empty | `authService.resendActivationToken("unknown")` | Assert `UserNotFoundException` |

---

### 1.2 `AuthControllerTest` (unit — standalone MockMvc)

**File:** `src/test/java/dev/thural/quietspace/authentication/controller/AuthControllerTest.java`

**Dependencies to mock:** `AuthService`

| # | Test | Endpoint | Act | Assert |
|---|---|---|---|---|
| 1 | `register_givenValidRequest_shouldReturn200` | `POST /api/v1/auth/register` | Valid `RegistrationRequest` JSON body | Status 200 |
| 2 | `register_givenMissingFields_shouldReturn400` | `POST /api/v1/auth/register` | Missing username, password < 8 chars | Status 400 |
| 3 | `register_givenInvalidEmail_shouldReturn400` | `POST /api/v1/auth/register` | Invalid email format | Status 400 |
| 4 | `authenticate_givenValidCredentials_shouldReturn200WithBody` | `POST /api/v1/auth/authenticate` | Valid `AuthRequest` JSON; `authService.authenticate()` → mock `AuthResponse` | Status 200 + response body matches mock |
| 5 | `authenticate_givenEmptyPassword_shouldReturn400` | `POST /api/v1/auth/authenticate` | Empty password | Status 400 |
| 6 | `activateAccount_givenValidToken_shouldReturn200` | `POST /api/v1/auth/activate-account` | Query param `token=abc` | Status 200 |
| 7 | `activateAccount_givenMissingToken_shouldReturn400` | `POST /api/v1/auth/activate-account` | No token param | Status 400 |
| 8 | `signout_givenAuthHeader_shouldReturn200` | `POST /api/v1/auth/signout` | `Authorization: Bearer ...` header | Status 200 |
| 9 | `refreshToken_givenAuthHeader_shouldReturn200WithBody` | `POST /api/v1/auth/refresh-token` | `Authorization: Bearer ...` header; `authService.refreshToken()` → mock response | Status 200 + body |
| 10 | `resendActivationEmail_givenValidEmail_shouldReturn200` | `POST /api/v1/auth/resend-code` | Query param `email=a@b.com` | Status 200 |
| 11 | `resendActivationEmail_givenMissingEmail_shouldReturn400` | `POST /api/v1/auth/resend-code` | No email param | Status 400 |

---

## Tier 2 — High (Photo & Notification)

### 2.1 `PhotoServiceImplTest`

**File:** `src/test/java/dev/thural/quietspace/service/impl/PhotoServiceImplTest.java`

**Dependencies to mock:** `CommonService`, `PhotoRepository`, `ImageCompressionUtil`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `uploadProfilePhoto_givenValidJpeg_shouldReturnPhotoName` | Return value is the saved photo's name; verify `photoRepository.save()` called with correct `entityId`, `entityType=USER`, `name` not null |
| 2 | `uploadProfilePhoto_givenFileExceeding2MB_shouldThrow` | `ImageUploadException` with "2MB" message |
| 3 | `uploadProfilePhoto_givenUnsupportedContentType_shouldThrow` | `UnsupportedImageTypeException` with "Unsupported image type" message; test with `"text/plain"`, `null` |
| 4 | `persistPhotoEntity_whenCompressionFails_shouldThrow` | `ImageCompressionUtil.compressImage()` throws `IOException` → `ImageUploadException` with "Failed to upload" message |
| 5 | `getPhotoByName_givenExistingName_shouldReturnResponse` | `PhotoResponse` with matching `name`, `type`, non-null `data` |
| 6 | `getPhotoByName_givenUnknownName_shouldThrow` | `EntityNotFoundException` containing "Photo not found with name" |
| 7 | `getPhotoByEntityId_givenExistingEntity_shouldReturnResponse` | `PhotoResponse` with matching fields |
| 8 | `getPhotoByEntityId_givenUnknownEntity_shouldThrow` | `EntityNotFoundException` |
| 9 | `getPhotoById_givenExistingPhoto_shouldReturnResponse` | `PhotoResponse` with matching fields |
| 10 | `getPhotoById_givenUnknownPhoto_shouldThrow` | `EntityNotFoundException` |
| 11 | `getPhotoData_givenExistingName_shouldReturnBytes` | Returned `byte[]` is decompressed data from `ImageCompressionUtil` |
| 12 | `getPhotoData_givenUnknownName_shouldThrow` | `EntityNotFoundException` |
| 13 | `deletePhotoByEntityId_shouldCallRepositoryDelete` | verify `photoRepository.deleteByEntityId(id)` called once |
| 14 | `deletePhotoById_shouldCallRepositoryDelete` | verify `photoRepository.deleteById(id)` called once |

---

### 2.2 `NotificationServiceImplTest`

**File:** `src/test/java/dev/thural/quietspace/service/impl/NotificationServiceImplTest.java`

**Dependencies to mock:** `NotificationRepository`, `NotificationMapper`, `UserService`, `CommentRepository`, `PostRepository`, `SimpMessagingTemplate`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `handleSeen_givenOwnNotification_shouldMarkSeenAndSendEvent` | Capture saved `Notification` → `isSeen=true`; verify `template.convertAndSendToUser()` called with recipient ID and `SEEN_NOTIFICATION` event |
| 2 | `handleSeen_givenOtherUsersNotification_shouldThrow` | Notification's `userId` differs from signed user → `ResourceAccessException` with "denied access" |
| 3 | `handleSeen_givenNonExistentNotification_shouldThrow` | `notificationRepository.findById()` → empty → `EntityNotFoundException` |
| 4 | `getAllNotifications_givenPagination_shouldReturnPage` | Returns mapped `Page<NotificationResponse>` |
| 5 | `getNotificationsByType_givenValidType_shouldReturnFilteredPage` | Returns mapped page filtered by `NotificationType` |
| 6 | `getNotificationsByType_givenInvalidTypeString_shouldThrow` | `"INVALID_TYPE"` → `IllegalArgumentException` from `NotificationType.valueOf()` |
| 7 | `getCountOfPendingNotifications_shouldReturnCount` | Returns integer from `repository.countByUserIdAndIsSeen()` |
| 8 | `processNotification_givenCommentType_shouldCreateAndSend` | Verify `notificationRepository.save()` called with correct `notificationType`, `contentId`, `actorId`, `userId`; verify `template.convertAndSendToUser()` called with `NOTIFICATION_SUBJECT_PATH` |
| 9 | `processNotification_givenPostReaction_shouldCreateAndSend` | Same — verify correct recipient resolved via post's user |
| 10 | `processNotification_givenFollowRequest_shouldUseContentIdAsRecipient` | Recipient ID equals `contentId` directly |
| 11 | `processNotification_whenWebSocketFails_shouldLogAndSwallow` | `template.convertAndSendToUser()` throws `MessagingException` → no exception propagates (logged only) |
| 12 | `processNotificationByReaction_givenComment_shouldProcessCommentReaction` | Delegates to `processNotification(COMMENT_REACTION, contentId)` — verify `notificationRepository.save()` called with `COMMENT_REACTION` type |
| 13 | `processNotificationByReaction_givenPost_shouldProcessPostReaction` | Same — `POST_REACTION` type |

---

### 2.3 `PhotoControllerTest` (unit — standalone MockMvc)

**File:** `src/test/java/dev/thural/quietspace/controller/PhotoControllerTest.java`

| # | Test | Endpoint | Assert |
|---|---|---|---|
| 1 | `uploadProfilePhoto_shouldReturn201WithName` | `POST /api/v1/photos/profile` | Status 201 + body is photo name string |
| 2 | `getPhotoByName_shouldReturn200WithImage` | `GET /api/v1/photos/{name}` | Status 200 + correct `Content-Type` header + body bytes |
| 3 | `getPhotoByName_whenNotFound_shouldPropagate404` | `GET /api/v1/photos/{name}` | `photoService.getPhotoByName()` throws `EntityNotFoundException` → status 404 (via exception handler) |
| 4 | `removePhotoByUserId_shouldReturn204` | `DELETE /api/v1/photos/profile/{userId}` | Status 204 |

### 2.4 `NotificationControllerTest` (unit — standalone MockMvc)

**File:** `src/test/java/dev/thural/quietspace/controller/NotificationControllerTest.java`

| # | Test | Endpoint | Assert |
|---|---|---|---|
| 1 | `handleSeen_shouldReturn202` | `POST /api/v1/notifications/seen/{id}` | Status 202 |
| 2 | `getAllNotifications_shouldReturnPage` | `GET /api/v1/notifications?page-number=0&page-size=10` | Status 200 + page body |
| 3 | `getNotificationsByType_shouldReturnFilteredPage` | `GET /api/v1/notifications/type/FOLLOW_REQUEST?page-number=0&page-size=10` | Status 200 |
| 4 | `getCountOfPendingNotifications_shouldReturn200WithCount` | `GET /api/v1/notifications/count-pending` | Status 200 + integer body |
| 5 | `processNotification_shouldReturn200` | `POST /api/v1/notifications/process` | Status 200 |

---

## Tier 3 — Medium (Admin, Reaction, Email, Common)

### 3.1 `AdminControllerTest` (unit — standalone MockMvc)

**File:** `src/test/java/dev/thural/quietspace/controller/AdminControllerTest.java`

| # | Test | Endpoint | Assert |
|---|---|---|---|
| 1 | `sayHello_shouldReturn200WithMessage` | `GET /api/v1/admin` | Status 200 + body "hello admin" |
| 2 | `deleteUserById_shouldReturn204` | `POST /api/v1/admin/{userId}` | Status 204; verify `userService.deleteUserById()` called |
| 3 | `getPagedUsers_shouldReturn200WithPage` | `GET /api/v1/admin/users?page-number=0&page-size=10` | Status 200 + page body |

### 3.2 `ReactionControllerTest` (unit — standalone MockMvc)

**File:** `src/test/java/dev/thural/quietspace/controller/ReactionControllerTest.java`

| # | Test | Endpoint | Assert |
|---|---|---|---|
| 1 | `getReactionsByUser_shouldReturn200WithPage` | `GET /api/v1/reactions/user?userId=...&contentType=POST` | Status 200 + page body |
| 2 | `getReactionsByContent_shouldReturn200WithPage` | `GET /api/v1/reactions/content?contentId=...&contentType=POST` | Status 200 + page body |
| 3 | `toggleReaction_shouldCallServiceAndNotificationThenReturn200` | `POST /api/v1/reactions/toggle-reaction` with valid `ReactionRequest` JSON | Status 200; verify `reactionService.handleReaction()` called; verify `notificationService.processNotificationByReaction()` called |
| 4 | `countByContentIdAndReactionType_shouldReturn200WithCount` | `GET /api/v1/reactions/count?contentId=...&type=LIKE` | Status 200 + integer body |

### 3.3 `EmailServiceTest`

**File:** `src/test/java/dev/thural/quietspace/service/impl/EmailServiceTest.java`

**Note:** This test requires careful mocking of `MimeMessage` and `MimeMessageHelper` internals. An alternative is to test at a higher level, but a plain Mockito test is acceptable.

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `sendEmail_givenAllParams_shouldSendMimeMessage` | Verify `mailSender.send(any(MimeMessage.class))` called once; verify helper `setTo`, `setSubject`, `setFrom` set correctly via template processing |
| 2 | `sendEmail_givenNullTemplate_shouldUseDefaultName` | When `emailTemplate` is null, template name `"confirm-email"` is used |
| 3 | `sendEmail_whenMailSenderFails_shouldPropagate` | `mailSender.send()` throws `MessagingException` → propagates |

### 3.4 `CommonServiceImplTest`

**File:** `src/test/java/dev/thural/quietspace/service/impl/CommonServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `getSignedUser_whenAuthenticated_shouldReturnUser` | `SecurityContextHolder` has authentication with username → `userRepository.findUserByUsername()` returns user → returned `User` is that user |
| 2 | `getSignedUser_whenUserNotFound_shouldThrow` | `findUserByUsername()` returns empty → `UserNotFoundException` |

---

## Tier 4 — Complete Partial Coverage

### 4.1 `UserServiceImplTest` — Add 13 Missing Methods

**File:** `src/test/java/dev/thural/quietspace/service/UserServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `queryUsers_givenUsernameAndPagination_shouldReturnPage` | Returns mapped `Page<UserResponse>` from `userQuery.findAllByQuery()` |
| 2 | `listUsersByUsername_givenSearchTerm_shouldReturnMatchingPage` | Delegates to `userRepository.findAllBySearchTerm()` |
| 3 | `listUsersByUsername_givenBlankSearchTerm_shouldReturnAll` | Falls back to `userRepository.findAll()` |
| 4 | `listFollowings_givenPublicProfile_shouldReturnPage` | Access check passes → returns paged followings |
| 5 | `listFollowings_givenPrivateAccountNotFollowed_shouldThrow` | `isPrivateAccount=true`, signed user not a follower → `UnauthorizedException` |
| 6 | `listFollowers_givenPublicProfile_shouldReturnPage` | Same as 4 but for followers |
| 7 | `toggleFollow_givenNotCurrentlyFollowing_shouldAdd` | After call, signed user's followings AND target's followers both contain each other |
| 8 | `toggleFollow_givenCurrentlyFollowing_shouldRemove` | After call, both lists no longer contain each other |
| 9 | `toggleFollow_givenSelfTarget_shouldThrow` | `followedUserId` == signed user's ID → `CustomErrorException` with "can't unfollow themselves" |
| 10 | `removeFollower_givenValidFollower_shouldRemove` | Follower removed from both `signedUser.followers` and `followingUser.followings` |
| 11 | `removeFollower_givenNotAFollower_shouldThrow` | `CustomErrorException` with "not found in followers" |
| 12 | `removeFollower_givenSelfTarget_shouldThrow` | `CustomErrorException` with "can't unfollow themselves" |
| 13 | `setOnlineStatus_givenExistingEmail_shouldSetUserOffline` | Captured user from repository has `statusType=OFFLINE` |
| 14 | `findConnectedFollowings_givenOnlineFollowings_shouldReturnList` | Only followings with `ONLINE` status appear in returned list |
| 15 | `saveProfileSettings_givenValidRequest_shouldCopyAndReturnResponse` | Returns `ProfileSettingsResponse` from mapper |
| 16 | `addUserToBlockList_givenExistingUser_shouldAddToBlocked` | Requested user appears in `signedUser.getProfileSettings().getBlockedUsers()` |

### 4.2 `PostServiceImplTest` — Add 6 Missing Methods

**File:** `src/test/java/dev/thural/quietspace/service/PostServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `patchPost_givenAuthor_shouldUpdateFieldsAndReturn` | Returned `PostResponse` has updated text/title; verify `photoService.deletePhotoByEntityId()` called if `photoData` is null |
| 2 | `patchPost_givenNonAuthor_shouldThrow` | Different user → `AccessDeniedException` |
| 3 | `getAllByQuery_givenSearchText_shouldReturnFilteredPage` | Uses specification with `containsText()` → returns mapped page |
| 4 | `getAllByQuery_givenNullSearchText_shouldReturnAllVisible` | Only `visibleToUser()` specification → returns mapped page |
| 5 | `addRepost_givenValidRequest_shouldSaveAndReturn` | `postRepository.save()` called with mapped entity; returns `PostResponse` |
| 6 | `getSavedPostsByUser_shouldReturnSavedPostsPage` | Uses `savedByUser()` specification → returns mapped page |
| 7 | `savePostForUser_givenExistingPost_shouldAddToSaved` | Post appears in `signedUser.getSavedPosts()` after call |
| 8 | `savePostForUser_givenNonExistentPost_shouldThrow` | Post not found → `EntityNotFoundException` |
| 9 | `getCommentedPostsByUserId_givenValidUser_shouldReturnPage` | Uses `commentedByUser()` specification → returns mapped page |

### 4.3 `MessageServiceImplTest` — Add 2 Missing Methods

**File:** `src/test/java/dev/thural/quietspace/service/MessageServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `setMessageSeen_givenExistingMessage_shouldSaveAndReturnSeenResponse` | Captured saved message has `isSeen=true`; returns `MessageResponse` |
| 2 | `setMessageSeen_givenNonExistentMessage_shouldThrow` | `EntityNotFoundException` |
| 3 | `getMessageById_givenValidIds_shouldReturnResponse` | Returns `MessageResponse` |
| 4 | `getMessageById_givenMismatchedIds_shouldThrow` | `EntityNotFoundException` |

### 4.4 `ChatServiceImplTest` — Add 1 Missing Method

**File:** `src/test/java/dev/thural/quietspace/service/ChatServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `getChatById_givenValidId_shouldReturnResponse` | Returns `ChatResponse` from mapper |
| 2 | `getChatById_givenNonExistentChat_shouldThrow` | `EntityNotFoundException` |

### 4.5 `CommentServiceImplTest` — Add 1 Missing Method

**File:** `src/test/java/dev/thural/quietspace/service/CommentServiceImplTest.java`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `getLatestCommentByUserIdAndPostId_givenExistingComment_shouldReturnResponse` | Returns `Optional<CommentResponse>` |
| 2 | `getLatestCommentByUserIdAndPostId_givenNoComment_shouldReturnEmpty` | Returns `Optional.empty()` |

---

## Tier 5 — Security & Utilities

### 5.1 `JwtServiceTest`

**File:** `src/test/java/dev/thural/quietspace/security/JwtServiceTest.java`

**Setup:** Use `ReflectionTestUtils.setField()` to inject a known `secretKey` (minimum 256-bit base64 for HMAC-SHA) and `jwtExpiration`/`jwtRefreshExpiration` values. Create a concrete `UserDetails` implementation for testing.

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `generateToken_givenUserDetails_shouldReturnValidJwt` | Returned string is non-null, has 3 dot-separated segments |
| 2 | `extractUsername_givenToken_shouldReturnSubject` | `token = generateToken(user)` → `extractUsername(token)` equals `user.getUsername()` |
| 3 | `isTokenValid_givenMatchingUser_shouldReturnTrue` | `isTokenValid(token, user)` → `true` |
| 4 | `isTokenValid_givenWrongUser_shouldReturnFalse` | Token for user A, checked against user B → `false` |
| 5 | `isTokenExpired_givenJustGeneratedToken_shouldReturnFalse` | `isTokenExpired(freshToken)` → `false` |
| 6 | `generateRefreshToken_shouldReturnTokenWithLongerExpiration` | Refresh token expiration is after access token expiration (verify via `extractExpiration`) |
| 7 | `extractClaim_givenCustomClaim_shouldReturnCorrectValue` | Token generated with extra claims → extract returns them |

### 5.2 `JwtFilterTest`

**File:** `src/test/java/dev/thural/quietspace/security/JwtFilterTest.java`

**Dependencies to mock:** `TokenRepository`, `UserDetailsService`, `JwtService`, `HttpServletRequest`, `HttpServletResponse`, `FilterChain`

| # | Test | Behavior asserted |
|---|---|---|
| 1 | `doFilter_givenNoAuthHeader_shouldSkipAndContinueChain` | `request.getHeader("Authorization")` → null → `filterChain.doFilter(request, response)` called, no auth set |
| 2 | `doFilter_givenNonBearerHeader_shouldSkip` | Header = `"Basic ..."` → same |
| 3 | `doFilter_givenBlacklistedToken_shouldSkip` | `tokenRepository.existsByToken()` → true → skip |
| 4 | `doFilter_givenNullUsername_shouldSkip` | `jwtService.extractUsername()` → null → skip |
| 5 | `doFilter_givenValidToken_shouldSetAuthenticationAndContinue` | All checks pass → `SecurityContextHolder.getContext().getAuthentication()` is non-null `UsernamePasswordAuthenticationToken` with correct principal; `filterChain.doFilter()` called |
| 6 | `doFilter_whenUserDetailsNotFound_shouldPropagate` | `userDetailsService.loadUserByUsername()` throws → exception propagates |

### 5.3 `PagingProviderTest`

**File:** `src/test/java/dev/thural/quietspace/utils/PagingProviderTest.java`

| # | Test | Assert |
|---|---|---|
| 1 | `buildPageRequest_givenNullPageNumber_returnsPage0` | `getPageNumber()` == 0 |
| 2 | `buildPageRequest_givenPageNumber3_returnsPage3` | `getPageNumber()` == 3 |
| 3 | `buildPageRequest_givenNullPageSize_returnsSize25` | `getPageSize()` == 25 |
| 4 | `buildPageRequest_givenPageSize2000_capsAt1000` | `getPageSize()` == 1000 |
| 5 | `buildPageRequest_givenNullSort_usesDefaultDescending` | Sort is `Sort.by("createDate").descending()` |
| 6 | `buildPageRequest_givenCustomSort_usesIt` | Sort matches the provided `Sort` object |

### 5.4 `PageUtilsTest`

**File:** `src/test/java/dev/thural/quietspace/utils/PageUtilsTest.java`

| # | Test | Assert |
|---|---|---|
| 1 | `pageFromList_givenFullPage_returnsSubListWithTotalCount` | Content size == page size, total elements == list size |
| 2 | `pageFromList_givenLastPartialPage_returnsRemaining` | Content size == remaining items, total == list size |
| 3 | `pageFromList_givenEmptyList_returnsEmptyPage` | Content empty, total == 0 |

### 5.5 `ImageCompressionUtilTest`

**File:** `src/test/java/dev/thural/quietspace/utils/ImageCompressionUtilTest.java`

**Note:** Uses real small test images loaded from test resources. Thumbnailator is a real library call — acceptable in a unit test since we're testing the utility's behavior with real image data.

| # | Test | Assert |
|---|---|---|
| 1 | `compressImage_givenValidJpeg_returnsSmallerBytes` | Output byte array length < input byte array length |
| 2 | `compressImage_givenZeroTargetSize_usesDefaultAndCompresses` | No exception; output is compressed |
| 3 | `compressImage_givenUnreadableStream_throwsIOException` | Null image data → `IOException` with "Unable to read image" |
| 4 | `decompressImage_givenByteArray_returnsSameBytes` | Input == output (pass-through) |
| 5 | `decompressImage_givenInputStream_returnsAllBytes` | All bytes read from stream |

---

## Summary of New Test Files

| Tier | File | Tests |
|---|---|---|
| 1 | `authentication/service/AuthServiceTest.java` | 18 |
| 1 | `authentication/controller/AuthControllerTest.java` | 11 |
| 2 | `service/impl/PhotoServiceImplTest.java` | 14 |
| 2 | `service/impl/NotificationServiceImplTest.java` | 13 |
| 2 | `controller/PhotoControllerTest.java` | 4 |
| 2 | `controller/NotificationControllerTest.java` | 5 |
| 3 | `controller/AdminControllerTest.java` | 3 |
| 3 | `controller/ReactionControllerTest.java` | 4 |
| 3 | `service/impl/EmailServiceTest.java` | 3 |
| 3 | `service/impl/CommonServiceImplTest.java` | 2 |
| 4 | `service/UserServiceImplTest.java` (extend) | 16 |
| 4 | `service/PostServiceImplTest.java` (extend) | 9 |
| 4 | `service/MessageServiceImplTest.java` (extend) | 4 |
| 4 | `service/ChatServiceImplTest.java` (extend) | 2 |
| 4 | `service/CommentServiceImplTest.java` (extend) | 2 |
| 5 | `security/JwtServiceTest.java` | 7 |
| 5 | `security/JwtFilterTest.java` | 6 |
| 5 | `utils/PagingProviderTest.java` | 6 |
| 5 | `utils/PageUtilsTest.java` | 3 |
| 5 | `utils/ImageCompressionUtilTest.java` | 5 |
| **Total** | **20 test files** | **~137 tests** |

---

## Estimated Coverage Impact

| Tier | Cumulative Coverage |
|---|---|
| Current baseline | 40% |
| After Tier 1 (Auth) | ~45% |
| After Tier 2 (Photo/Notification) | ~52% |
| After Tier 3 (Admin/Reaction/Email) | ~55% |
| After Tier 4 (Fill gaps) | ~62% |
| After Tier 5 (Security/Utils) | **~68–72%** |

---

## Prerequisite

Resolve the **57 failing tests** before writing new tests. Likely root causes:
- Integration/slice tests depending on database state
- Missing test profiles or environment config
- Flaky `@DataJpaTest` ordering dependencies

Run `./gradlew test` continuously during implementation to prevent regressions.
