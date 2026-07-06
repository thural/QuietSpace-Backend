from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.chat import Chat
from app.repositories.chat import ChatRepository
from app.schemas.chat import ChatCreate


class ChatService:
    def __init__(self, session: AsyncSession):
        self.chat_repo = ChatRepository(session)

    async def create_chat(self, chat_in: ChatCreate, creator_id: UUID) -> Chat:
        chat = Chat(name=chat_in.name, is_group=chat_in.is_group)
        chat = await self.chat_repo.create(chat)
        return chat

    async def get_chat(self, chat_id: UUID) -> Chat | None:
        return await self.chat_repo.get(chat_id)

    async def get_user_chats(self, user_id: UUID) -> list[Chat]:
        return await self.chat_repo.get_by_participant(user_id)
