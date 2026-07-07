from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.models.message import Message
from app.repositories.message import MessageRepository
from app.repositories.blocked_user import BlockedUserRepository


class MessageService:
    def __init__(self, session: AsyncSession):
        self.message_repo = MessageRepository(session)
        self.block_repo = BlockedUserRepository(session)

    async def add_message(self, message_data: dict) -> Message:
        sender_id = message_data.get("sender_id")
        recipient_id = message_data.get("recipient_id")
        if sender_id and recipient_id:
            if await self.block_repo.is_blocked(sender_id, recipient_id):
                raise ValueError("Cannot send message to a user you have blocked")
            if await self.block_repo.is_blocked(recipient_id, sender_id):
                raise ValueError("You have been blocked by this user")
        message = Message(**message_data)
        return await self.message_repo.create(message)

    async def get_messages(self, chat_id: UUID, limit: int = 50, offset: int = 0, current_user_id: UUID | None = None) -> list[Message]:
        messages = await self.message_repo.get_by_chat(chat_id, limit, offset)
        if current_user_id:
            messages = await self._filter_blocked_messages(messages, current_user_id)
        return messages

    async def get_unread(self, user_id: UUID) -> list[Message]:
        messages = await self.message_repo.get_unread(user_id)
        return await self._filter_blocked_messages(messages, user_id)

    async def mark_as_read(self, message_id: UUID, user_id: UUID) -> tuple[UUID, UUID] | None:
        message = await self.message_repo.get(message_id)
        if not message:
            return None
        if message.recipient_id != user_id:
            return None
        if not message.read:
            message.read = True
            message.read_at = datetime.utcnow()
            await self.message_repo.update(message)
        return message.sender_id, message.chat_id

    async def delete_message(self, message_id: UUID, user_id: UUID) -> UUID:
        message = await self.message_repo.get(message_id)
        if not message:
            raise ValueError("Message not found")
        if message.sender_id != user_id:
            raise ValueError("Not authorized to delete this message")
        await self.message_repo.delete(message_id)
        return message.chat_id

    async def _filter_blocked_messages(self, messages: list[Message], user_id: UUID) -> list[Message]:
        blocked_ids = await self.block_repo.get_blocked_ids(user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(user_id)
        excluded = blocked_ids | blocker_ids
        return [
            m for m in messages
            if m.sender_id not in excluded and m.recipient_id not in excluded
        ]
