from contextlib import asynccontextmanager
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlmodel import SQLModel
from sqlalchemy import text
from app.config.settings import settings
from app.core.exceptions import register_exception_handlers
from app.api.v1.router import api_router
from app.api.websocket.socketio import socketio_app
import structlog

logger = structlog.get_logger()


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("Starting application...")
    engine = create_async_engine(
        settings.DATABASE_URL,
        pool_size=10,
        max_overflow=20,
        pool_pre_ping=True,
        pool_recycle=3600,
        echo=settings.DEBUG,
    )
    async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    app.state.engine = engine
    app.state.async_session = async_session
    yield
    logger.info("Shutting down application...")
    await engine.dispose()


app = FastAPI(lifespan=lifespan)
register_exception_handlers(app)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.FRONTEND_URL],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

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
    return {
        "status": "healthy" if db_status == "healthy" else "unhealthy",
        "database": db_status,
        "version": "1.0.0",
    }
