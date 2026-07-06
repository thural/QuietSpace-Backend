from socketio import AsyncServer, ASGIApp, AsyncRedisManager
from app.config.settings import settings

redis_manager = AsyncRedisManager(settings.REDIS_URL)

socketio = AsyncServer(
    async_mode="asgi",
    cors_allowed_origins=[settings.FRONTEND_URL],
    client_manager=redis_manager,
    logger=True,
    engineio_logger=True,
)

socketio_app = ASGIApp(socketio)
