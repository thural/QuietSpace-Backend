# API Improvement Test Plan

Unit, slice, and integration tests for all new endpoints and service methods added in the API improvement plan.

---

## Phase 1 — Unit Tests (Service Layer)

### Step 1.1: Follow/Unfollow
- `UserServiceImplTest.followUser_shouldFollowUser()`
- `UserServiceImplTest.followUser_givenSelf_shouldThrow()`
- `UserServiceImplTest.followUser_givenAlreadyFollowing_shouldBeNoOp()`
- `UserServiceImplTest.unfollowUser_shouldUnfollowUser()`
- `UserServiceImplTest.unfollowUser_givenSelf_shouldThrow()`
- `UserServiceImplTest.unfollowUser_givenNotFollowing_shouldBeNoOp()`

### Step 1.2: Block/Unblock
- `UserServiceImplTest.removeUserFromBlockList_shouldRemoveBlockedUser()`
- `UserServiceImplTest.removeUserFromBlockList_givenNotBlocked_shouldBeNoOp()`
- `UserServiceImplTest.getBlockedUsers_shouldReturnBlockedUsersList()`
- `UserServiceImplTest.getBlockedUsers_givenNone_shouldReturnEmpty()`

### Step 1.3: Admin disable
- `UserServiceImplTest.disableUser_shouldDisableUser()`
- `UserServiceImplTest.disableUser_givenNonExistent_shouldThrow()`

### Step 1.4: Chat update
- `ChatServiceImplTest.updateChat_shouldUpdateName()`
- `ChatServiceImplTest.updateChat_givenNullName_shouldNotChange()`

### Step 1.5: Message unread + read push
- `MessageServiceImplTest.getUnreadCount_shouldReturnCount()`
- `MessageServiceImplTest.setMessageSeen_shouldPushUnreadCount()`

### Step 1.6: Reaction add/remove
- `ReactionServiceImplTest.addReaction_shouldCreateNewReaction()`
- `ReactionServiceImplTest.addReaction_givenExisting_shouldUpdateType()`
- `ReactionServiceImplTest.removeReaction_shouldDeleteReaction()`

### Step 1.7: Post unsave
- `PostServiceImplTest.unsavePostForUser_shouldRemoveSavedPost()`

### Step 1.8: Photo upload
- `PhotoServiceImplTest.uploadPhoto_shouldPersistWithNullEntityType()`

### Step 1.9: Notification unread push
- `NotificationServiceImplTest.handleSeen_shouldPushUnreadCount()`
- `NotificationServiceImplTest.processNotification_shouldPushUnreadCount()`

---

## Phase 2 — Controller Slice Tests (Web Layer)

### Step 2.1: Chat controller
- `ChatControllerSliceTest.getMyChats_shouldReturn200()`
- `ChatControllerSliceTest.updateChat_shouldReturn200()`

### Step 2.2: Message controller
- `MessageControllerSliceTest.getUnreadCount_shouldReturn200()`
- `MessageControllerSliceTest.markAsRead_shouldReturn200()`
- `MessageControllerSliceTest.markAsRead_givenNonExistent_shouldReturn404()`

### Step 2.3: User controller
- A new slice test file or additions to existing:
  - `getMe_shouldReturn200()`
  - `patchMe_shouldReturn200()`
  - `followUser_shouldReturn200()`
  - `unfollowUser_shouldReturn204()`
  - `unblockUserProfile_shouldReturn204()`
  - `getBlockedUsers_shouldReturn200()`
  - `getOnlineUsers_shouldReturn200()`

### Step 2.4: Notification controller
- `NotificationControllerSliceTest.markAsRead_shouldReturn200()`

### Step 2.5: Reaction controller
- `ReactionControllerSliceTest.getReactionsByPostId_shouldReturn200()`
- `ReactionControllerSliceTest.addReaction_shouldReturn200()`
- `ReactionControllerSliceTest.removeReaction_shouldReturn204()`

### Step 2.6: Photo controller
- `PhotoControllerSliceTest.uploadPhoto_shouldReturn201()`
- `PhotoControllerSliceTest.deletePhotoById_shouldReturn204()`

### Step 2.7: Post controller
- `PostControllerSliceTest.savePostForUser_shouldReturn200()`
- `PostControllerSliceTest.unsavePostForUser_shouldReturn204()`

### Step 2.8: Admin controller
- `AdminControllerSliceTest.disableUser_shouldReturn200()`

### Step 2.9: Auth controller
- `AuthControllerSliceTest.login_shouldReturn200()`
- `AuthControllerSliceTest.logout_shouldReturn200()`

### Step 2.10: Health controller
- `HealthControllerTest.health_shouldReturn200()`

---

## Phase 3 — Integration Tests

### Step 3.1: Follow/unfollow lifecycle
- Add to `UserFlowIT`: create users, follow, verify following list, unfollow, verify removed

### Step 3.2: Block/unblock lifecycle
- Add to `UserFlowIT`: block user, verify blocked list, unblock, verify removed

### Step 3.3: Message unread lifecycle
- Add to a new or existing flow test: send message, verify unread count, mark read, verify count

### Step 3.4: Mark notification as read
- Add to notification flow: create notification, mark read, verify count
