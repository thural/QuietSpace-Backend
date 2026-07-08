from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, and_
from uuid import UUID
from datetime import datetime, timezone
from app.models.poll import Poll, PollOption, PollVote
from app.repositories.base import BaseRepository


class PollRepository(BaseRepository[Poll]):
    def __init__(self, session: AsyncSession):
        super().__init__(Poll, session)

    async def get_by_post_id(self, post_id: UUID) -> Poll | None:
        result = await self.session.execute(
            select(Poll).where(Poll.post_id == post_id)
        )
        return result.scalar_one_or_none()

    async def create_poll(self, post_id: UUID, question: str, expires_at: datetime | None = None) -> Poll:
        poll = Poll(post_id=post_id, question=question, expires_at=expires_at)
        self.session.add(poll)
        return poll

    async def get_results(self, poll_id: UUID) -> dict:
        result = await self.session.execute(
            select(
                PollOption.id,
                PollOption.option_text,
                PollOption.vote_count,
                func.count(PollVote.id).label("total_votes")
            )
            .outerjoin(PollVote, PollVote.poll_option_id == PollOption.id)
            .where(PollOption.poll_id == poll_id)
            .group_by(PollOption.id, PollOption.option_text, PollOption.vote_count)
        )
        rows = result.all()
        return {
            "options": [
                {
                    "id": str(row.id),
                    "text": row.option_text,
                    "vote_count": row.vote_count,
                    "total_votes": row.total_votes or 0
                }
                for row in rows
            ],
            "total_votes": sum(row.total_votes or 0 for row in rows)
        }


class PollOptionRepository(BaseRepository[PollOption]):
    def __init__(self, session: AsyncSession):
        super().__init__(PollOption, session)

    async def get_by_poll_id(self, poll_id: UUID) -> list[PollOption]:
        result = await self.session.execute(
            select(PollOption).where(PollOption.poll_id == poll_id)
        )
        return result.scalars().all()

    async def add_option(self, poll_id: UUID, option_text: str) -> PollOption:
        option = PollOption(poll_id=poll_id, option_text=option_text, vote_count=0)
        self.session.add(option)
        return option

    async def increment_vote_count(self, option_id: UUID) -> PollOption | None:
        result = await self.session.execute(
            select(PollOption).where(PollOption.id == option_id)
        )
        option = result.scalar_one_or_none()
        if option:
            option.vote_count += 1
        return option


class PollVoteRepository(BaseRepository[PollVote]):
    def __init__(self, session: AsyncSession):
        super().__init__(PollVote, session)

    async def has_voted(self, poll_id: UUID, user_id: UUID) -> bool:
        result = await self.session.execute(
            select(PollVote)
            .join(PollOption, PollVote.poll_option_id == PollOption.id)
            .where(and_(PollOption.poll_id == poll_id, PollVote.user_id == user_id))
        )
        return result.scalar_one_or_none() is not None

    async def vote(self, poll_option_id: UUID, user_id: UUID) -> PollVote:
        vote = PollVote(poll_option_id=poll_option_id, user_id=user_id, voted_at=datetime.now(timezone.utc))
        self.session.add(vote)
        return vote

    async def get_user_vote(self, poll_id: UUID, user_id: UUID) -> PollVote | None:
        result = await self.session.execute(
            select(PollVote)
            .join(PollOption, PollVote.poll_option_id == PollOption.id)
            .where(and_(PollOption.poll_id == poll_id, PollVote.user_id == user_id))
        )
        return result.scalar_one_or_none()
