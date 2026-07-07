import time
import structlog
from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import text
from slowapi.errors import RateLimitExceeded
from app.config.settings import settings
from app.core.exceptions import register_exception_handlers
from app.core.rate_limiter import limiter
from app.api.v1.router import api_router
from app.api.websocket.socketio import socketio_app

structlog.configure(
    processors=[
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.dev.ConsoleRenderer()
        if settings.DEBUG
        else structlog.processors.JSONRenderer(),
    ],
    wrapper_class=structlog.make_filtering_bound_logger(20),
    context_class=dict,
    logger_factory=structlog.PrintLoggerFactory(),
    cache_logger_on_first_use=True,
)

logger = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting application...")
    from app.config.database import engine, async_session
    from app.config.redis import redis_client
    from app.core.cache import CacheService
    app.state.engine = engine
    app.state.async_session = async_session
    app.state.redis = redis_client
    app.state.cache_service = CacheService(redis_client)
    app.state.limiter = limiter
    yield
    logger.info("Shutting down application...")
    await engine.dispose()
    await redis_client.close()


app = FastAPI(lifespan=lifespan, redirect_slashes=False)
register_exception_handlers(app)

app.state.limiter = limiter


async def rate_limit_exceeded_handler(request: Request, exc: RateLimitExceeded):
    structlog.contextvars.bind_contextvars(
        path=request.url.path,
        method=request.method,
        client_host=request.client.host if request.client else "unknown",
    )
    logger.warning("rate_limit_exceeded", detail=str(exc))
    from fastapi.responses import JSONResponse
    return JSONResponse(
        status_code=429,
        content={"detail": "Rate limit exceeded. Please try again later."},
    )


app.add_exception_handler(RateLimitExceeded, rate_limit_exceeded_handler)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.FRONTEND_URL],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.middleware("http")
async def logging_middleware(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    duration = time.time() - start_time
    structlog.contextvars.bind_contextvars(
        method=request.method,
        path=request.url.path,
        status_code=response.status_code,
        duration_ms=round(duration * 1000, 2),
    )
    logger.info("request_completed")
    return response


app.include_router(api_router, prefix="/api/v1")
app.mount("/ws", socketio_app)


@app.get("/health", tags=["Health"])
async def health_check(request: Request):
    try:
        async with app.state.async_session() as session:
            await session.execute(text("SELECT 1"))
            db_status = "healthy"
    except Exception:
        db_status = "unhealthy"
    try:
        await app.state.redis.ping()
        redis_status = "healthy"
    except Exception:
        redis_status = "unhealthy"
    overall = "healthy" if db_status == "healthy" and redis_status == "healthy" else "unhealthy"
    return {
        "status": overall,
        "database": db_status,
        "redis": redis_status,
        "version": "1.0.0",
    }
