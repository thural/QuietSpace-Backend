from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.chat import Chat
from app.repositories.base import BaseRepository


class ChatRepository(BaseRepository[Chat]):
    def __init__(self, session: AsyncSession):
        super().__init__(Chat, session)

    async def get_by_participant(self, user_id: UUID) -> list[Chat]:
        from app.models.chat_participant import ChatParticipant
        result = await self.session.execute(
            select(Chat)
            .join(ChatParticipant, ChatParticipant.chat_id == Chat.id)
            .where(ChatParticipant.user_id == user_id)
            .order_by(Chat.updated_at.desc())
        )
        return result.scalars().all()


chat_repository = ChatRepository
