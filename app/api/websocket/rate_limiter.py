from uuid import UUID
from app.config.redis import redis_client


class WebSocketRateLimiter:
    async def check(self, user_id: UUID, event: str, max_burst: int, per_seconds: int) -> bool:
        key = f"ws_rate:{user_id}:{event}"
        current = await redis_client.incr(key)
        if current == 1:
            await redis_client.expire(key, per_seconds)
        return current <= max_burst


rate_limiter = WebSocketRateLimiter()
