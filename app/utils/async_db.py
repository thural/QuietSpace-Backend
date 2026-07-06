from inspect import iscoroutinefunction
from sqlalchemy.ext.asyncio import AsyncSession


def verify_async_session(session) -> bool:
    """
    Verify that the provided session is an async session instance.
    Returns True if valid, False otherwise.
    """
    return isinstance(session, AsyncSession)


def ensure_async(func):
    """
    Decorator that raises a RuntimeError if a function
    is accidentally defined as synchronous in an async context.
    Use on WebSocket event handlers to prevent blocking calls.
    """
    if not iscoroutinefunction(func):
        raise RuntimeError(
            f"{func.__name__} must be an async function. "
            "Blocking calls are not allowed in WebSocket handlers."
        )
    return func
