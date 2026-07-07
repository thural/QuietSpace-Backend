from slowapi import Limiter
from slowapi.util import get_remote_address
from app.config.settings import settings

AUTH_LIMIT = "5/minute"
CONTENT_LIMIT = "10/minute"
SENSITIVE_LIMIT = "10/minute"
DEFAULT_LIMIT = "100/minute"
RESEND_CODE_LIMIT = "3/5minutes"

limiter = Limiter(
    key_func=get_remote_address,
    storage_uri=settings.REDIS_URL,
    default_limits=[DEFAULT_LIMIT],
)
