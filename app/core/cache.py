from typing import Any, Callable, Optional
import pickle
import structlog
from redis.asyncio import Redis

logger = structlog.get_logger()

DEFAULT_TTL = 300


class CacheService:
    def __init__(self, redis: Redis):
        self.redis = redis

    async def get(self, key: str) -> Any | None:
        data = await self.redis.get(key)
        if data is None:
            return None
        try:
            return pickle.loads(data)
        except Exception:
            await self.redis.delete(key)
            return None

    async def set(self, key: str, value: Any, ttl: int = DEFAULT_TTL) -> None:
        data = pickle.dumps(value)
        await self.redis.setex(key, ttl, data)

    async def delete(self, key: str) -> None:
        await self.redis.delete(key)

    async def invalidate_pattern(self, pattern: str) -> None:
        cursor = 0
        while True:
            cursor, keys = await self.redis.scan(cursor=cursor, match=pattern, count=100)
            if keys:
                await self.redis.delete(*keys)
            if cursor == 0:
                break

    async def get_or_set(
        self, key: str, factory: Callable, ttl: int = DEFAULT_TTL, *args, **kwargs
    ) -> Any:
        cached = await self.get(key)
        if cached is not None:
            return cached
        value = await factory(*args, **kwargs)
        await self.set(key, value, ttl)
        return value
