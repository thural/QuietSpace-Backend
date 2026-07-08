import structlog
from uuid import UUID
from datetime import datetime
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.chat import Chat
from app.models.chat_participant import ChatParticipant
from app.repositories.chat import ChatRepository
from app.schemas.chat import ChatCreate, ChatUpdate
from app.core.unit_of_work import UnitOfWork
from app.models.websocket_event import EventFactory
from app.enums.websocket_event_type import WebSocketEventType

logger = structlog.get_logger()


class ChatService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.chat_repo = ChatRepository(session)

    async def create_chat(self, chat_in: ChatCreate, creator_id: UUID) -> Chat:
        chat = Chat(name=chat_in.name, is_group=chat_in.is_group)
        chat = await self.chat_repo.create(chat)
        return chat

    async def get_chat(self, chat_id: UUID) -> Chat | None:
        return await self.chat_repo.get(chat_id)

    async def get_user_chats(self, user_id: UUID, cursor: str | None = None, limit: int = 20) -> tuple[list[Chat], str | None, bool]:
        return await self.chat_repo.get_by_participant(user_id, cursor, limit)

    async def add_member(self, chat_id: UUID, user_id: UUID, actor_id: UUID) -> bool:
        result = await self.session.execute(
            select(ChatParticipant).where(
                ChatParticipant.chat_id == chat_id,
                ChatParticipant.user_id == user_id,
            )
        )
        if result.scalar_one_or_none():
            return False
        participant = ChatParticipant(chat_id=chat_id, user_id=user_id)
        self.session.add(participant)
        return True

    async def remove_member(self, chat_id: UUID, user_id: UUID) -> bool:
        result = await self.session.execute(
            select(ChatParticipant).where(
                ChatParticipant.chat_id == chat_id,
                ChatParticipant.user_id == user_id,
            )
        )
        participant = result.scalar_one_or_none()
        if not participant:
            return False
        await self.session.delete(participant)
        return True

    async def delete_chat(self, chat_id: UUID, user_id: UUID) -> Chat | None:
        chat = await self.chat_repo.get(chat_id)
        if not chat:
            return None
        result = await self.session.execute(
            select(ChatParticipant).where(
                ChatParticipant.chat_id == chat_id,
                ChatParticipant.user_id == user_id,
            )
        )
        if not result.scalar_one_or_none():
            raise ValueError("Not a chat member")
        deleted = await self.chat_repo.soft_delete(chat_id)
        if not deleted:
            return None
        logger.info("chat_soft_deleted", chat_id=str(chat_id), user_id=str(user_id))
        return deleted

    async def update_chat(self, chat_id: UUID, chat_in: ChatUpdate) -> Chat | None:
        chat = await self.chat_repo.get(chat_id)
        if not chat:
            return None
        update_data = chat_in.model_dump(exclude_unset=True)
        for field, value in update_data.items():
            setattr(chat, field, value)
        return await self.chat_repo.update(chat)
