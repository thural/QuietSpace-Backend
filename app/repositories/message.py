from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.message import Message
from app.repositories.base import BaseRepository


class MessageRepository(BaseRepository[Message]):
    def __init__(self, session: AsyncSession):
        super().__init__(Message, session)

    async def get_by_chat(self, chat_id: UUID, limit: int = 50, offset: int = 0) -> list[Message]:
        result = await self.session.execute(
            select(Message)
            .where(Message.chat_id == chat_id)
            .order_by(Message.created_at.desc())
            .limit(limit)
            .offset(offset)
        )
        return result.scalars().all()

    async def get_unread(self, user_id: UUID) -> list[Message]:
        result = await self.session.execute(
            select(Message)
            .where(Message.recipient_id == user_id, Message.read == False)
            .order_by(Message.created_at.asc())
        )
        return result.scalars().all()


message_repository = MessageRepository
