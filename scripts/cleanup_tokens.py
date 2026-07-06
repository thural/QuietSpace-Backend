#!/usr/bin/env python3
import asyncio
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker
from app.config.settings import settings
from app.models.token import Token


async def cleanup():
    engine = create_async_engine(settings.DATABASE_URL)
    session = async_sessionmaker(engine)
    async with session() as db:
        result = await db.execute("DELETE FROM token WHERE expires_at < NOW()")
        await db.commit()
    await engine.dispose()


if __name__ == "__main__":
    asyncio.run(cleanup())
