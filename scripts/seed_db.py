#!/usr/bin/env python3
import asyncio
import sys
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker
from app.config.settings import settings
from app.models.user import User
from app.models.post import Post
from app.enums.role import Role
from app.enums.status_type import StatusType


async def seed():
    engine = create_async_engine(settings.DATABASE_URL)
    session = async_sessionmaker(engine)
    async with session() as db:
        user = User(
            username="admin",
            email="admin@example.com",
            password_hash="hashed",
            role=Role.ADMIN,
            status=StatusType.ACTIVE,
        )
        db.add(user)
        await db.commit()
    await engine.dispose()


if __name__ == "__main__":
    asyncio.run(seed())
