from fastapi import APIRouter
from app.api.v1 import auth, users, posts, comments, chats, messages, notifications, reactions, photos, admin

api_router = APIRouter()

api_router.include_router(auth.router, prefix="/auth", tags=["Auth"])
api_router.include_router(users.router, prefix="/users", tags=["Users"])
api_router.include_router(posts.router, prefix="/posts", tags=["Posts"])
api_router.include_router(comments.router, prefix="/comments", tags=["Comments"])
api_router.include_router(chats.router, prefix="/chats", tags=["Chats"])
api_router.include_router(messages.router, prefix="/messages", tags=["Messages"])
api_router.include_router(notifications.router, prefix="/notifications", tags=["Notifications"])
api_router.include_router(reactions.router, prefix="/reactions", tags=["Reactions"])
api_router.include_router(photos.router, prefix="/photos", tags=["Photos"])
api_router.include_router(admin.router, prefix="/admin", tags=["Admin"])
