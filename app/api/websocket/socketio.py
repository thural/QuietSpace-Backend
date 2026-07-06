from socketio import AsyncServer, ASGIApp
from app.config.settings import settings

socketio = AsyncServer(
    async_mode="asgi",
    cors_allowed_origins=[settings.FRONTEND_URL],
    logger=True,
    engineio_logger=True,
)

socketio_app = ASGIApp(socketio)
