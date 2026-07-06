from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.message import Message
from app.repositories.message import MessageRepository
from app.schemas.message import MessageCreate


class MessageService:
    def __init__(self, session: AsyncSession):
        self.message_repo = MessageRepository(session)

    async def add_message(self, message_data: dict) -> Message:
        message = Message(**message_data)
        return await self.message_repo.create(message)

    async def get_messages(self, chat_id: UUID, limit: int = 50, offset: int = 0) -> list[Message]:
        return await self.message_repo.get_by_chat(chat_id, limit, offset)

    async def get_unread(self, user_id: UUID) -> list[Message]:
        return await self.message_repo.get_unread(user_id)
