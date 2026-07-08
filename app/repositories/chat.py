from datetime import datetime, timezone
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID, uuid5, NAMESPACE_DNS
from app.models.chat import Chat
from app.repositories.base import BaseRepository

PUBLIC_CHAT_ID = uuid5(NAMESPACE_DNS, "quietspace.public.chat")


class ChatRepository(BaseRepository[Chat]):
    def __init__(self, session: AsyncSession):
        super().__init__(Chat, session)

    async def get_by_participant(self, user_id: UUID, cursor: str | None = None, limit: int = 20) -> tuple[list[Chat], str | None, bool]:
        from app.models.chat_participant import ChatParticipant
        stmt = (
            select(Chat)
            .join(ChatParticipant, ChatParticipant.chat_id == Chat.id)
            .where(ChatParticipant.user_id == user_id)
            .order_by(Chat.updated_at.desc())
        )
        return await self.paginate_cursor(stmt, cursor, limit)

    async def soft_delete(self, chat_id: UUID) -> Chat | None:
        chat = await self.get(chat_id)
        if not chat:
            return None
        chat.deleted_at = datetime.now(timezone.utc)
        return await self.update(chat)

    async def get_or_create_public_chat(self) -> Chat:
        chat = await self.get(PUBLIC_CHAT_ID)
        if chat:
            return chat
        chat = Chat(
            id=PUBLIC_CHAT_ID,
            name="Public Room",
            is_group=True,
        )
        return await self.create(chat)


chat_repository = ChatRepository
