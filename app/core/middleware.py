import time
import structlog
from fastapi import Request, Response


async def timing_middleware(request: Request, call_next):
    start_time = time.time()
    response: Response = await call_next(request)
    duration = time.time() - start_time
    response.headers["X-Process-Time-MS"] = str(round(duration * 1000, 2))
    return response
