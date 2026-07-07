import redis.asyncio as aioredis
from app.config.settings import settings

redis_client = aioredis.from_url(
    settings.REDIS_URL,
    decode_responses=True,
    max_connections=settings.REDIS_POOL_SIZE,
)
