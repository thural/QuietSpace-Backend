from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from datetime import datetime
from app.models.poll import Poll, PollOption
from app.repositories.poll import PollRepository, PollOptionRepository, PollVoteRepository
from app.schemas.poll import PollCreate, VoteRequest
from app.core.unit_of_work import UnitOfWork


class PollService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.poll_repo = PollRepository(session)
        self.option_repo = PollOptionRepository(session)
        self.vote_repo = PollVoteRepository(session)

    async def create_poll(self, post_id: UUID, poll_in: PollCreate) -> Poll:
        async with UnitOfWork(self.session) as uow:
            poll = await self.poll_repo.create_poll(
                post_id=post_id,
                question=poll_in.question,
                expires_at=poll_in.expires_at
            )
            await self.session.flush()
            
            for option_in in poll_in.options:
                await self.option_repo.add_option(poll.id, option_in.option_text)
            
            await uow.commit()
            await self.session.refresh(poll)
            return poll

    async def vote(self, user_id: UUID, vote_in: VoteRequest) -> bool:
        async with UnitOfWork(self.session) as uow:
            option = await self.option_repo.get(vote_in.poll_option_id)
            if not option:
                raise HTTPException(
                    status_code=status.HTTP_404_NOT_FOUND,
                    detail="Poll option not found"
                )
            
            if await self.vote_repo.has_voted(option.poll_id, user_id):
                raise HTTPException(
                    status_code=status.HTTP_400_BAD_REQUEST,
                    detail="User has already voted on this poll"
                )
            
            await self.vote_repo.vote(vote_in.poll_option_id, user_id)
            await self.option_repo.increment_vote_count(vote_in.poll_option_id)
            
            await uow.commit()
            return True

    async def get_poll_by_post_id(self, post_id: UUID) -> Poll | None:
        poll = await self.poll_repo.get_by_post_id(post_id)
        if poll:
            await self.session.refresh(poll, attribute_names=["options"])
        return poll

    async def get_results(self, poll_id: UUID) -> dict:
        return await self.poll_repo.get_results(poll_id)

    async def has_user_voted(self, poll_id: UUID, user_id: UUID) -> bool:
        return await self.vote_repo.has_voted(poll_id, user_id)
