from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from app.models.poll import Poll, PollOption
from app.repositories.base import BaseRepository


class PollRepository(BaseRepository[Poll]):
    def __init__(self, session: AsyncSession):
        super().__init__(Poll, session)

    async def get_by_post_id(self, post_id: UUID) -> list[Poll]:
        result = await self.session.execute(
            select(Poll).where(Poll.post_id == post_id)
        )
        return result.scalars().all()


class PollOptionRepository(BaseRepository[PollOption]):
    def __init__(self, session: AsyncSession):
        super().__init__(PollOption, session)

    async def get_by_poll_id(self, poll_id: UUID) -> list[PollOption]:
        result = await self.session.execute(
            select(PollOption).where(PollOption.poll_id == poll_id)
        )
        return result.scalars().all()
