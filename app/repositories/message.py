from datetime import datetime
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.message import Message
from app.repositories.base import BaseRepository


class MessageRepository(BaseRepository[Message]):
    def __init__(self, session: AsyncSession):
        super().__init__(Message, session)

    async def get_by_chat(self, chat_id: UUID, limit: int = 50, offset: int = 0, include_deleted: bool = False) -> list[Message]:
        stmt = select(Message).where(Message.chat_id == chat_id)
        if not include_deleted:
            stmt = stmt.where(Message.deleted_at.is_(None))
        stmt = stmt.order_by(Message.created_at.desc()).limit(limit).offset(offset)
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def get_unread(self, user_id: UUID, include_deleted: bool = False) -> list[Message]:
        stmt = select(Message).where(Message.recipient_id == user_id, Message.read == False)
        if not include_deleted:
            stmt = stmt.where(Message.deleted_at.is_(None))
        stmt = stmt.order_by(Message.created_at.asc())
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def soft_delete(self, message_id: UUID) -> Message | None:
        message = await self.get(message_id)
        if not message:
            return None
        message.deleted_at = datetime.utcnow()
        return await self.update(message)


message_repository = MessageRepository
