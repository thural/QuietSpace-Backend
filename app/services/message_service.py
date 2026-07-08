from datetime import datetime
import structlog
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.enums.role import Role
from app.models.message import Message
from app.repositories.message import MessageRepository
from app.repositories.blocked_user import BlockedUserRepository

logger = structlog.get_logger()


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

    async def get_messages(self, chat_id: UUID, cursor: str | None = None, limit: int = 50, current_user_id: UUID | None = None) -> tuple[list[Message], str | None, bool]:
        messages, next_cursor, has_more = await self.message_repo.get_by_chat(chat_id, cursor, limit)
        if current_user_id:
            messages = await self._filter_blocked_messages(messages, current_user_id)
        return messages, next_cursor, has_more

    async def get_message(self, message_id: UUID, current_user_id: UUID | None = None) -> Message | None:
        message = await self.message_repo.get(message_id)
        if not message:
            return None
        if current_user_id and message.sender_id != current_user_id and message.recipient_id != current_user_id:
            return None
        return message

    async def get_unread(self, user_id: UUID, cursor: str | None = None, limit: int = 20) -> tuple[list[Message], str | None, bool]:
        messages, next_cursor, has_more = await self.message_repo.get_unread(user_id, cursor, limit)
        messages = await self._filter_blocked_messages(messages, user_id)
        return messages, next_cursor, has_more

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

    async def delete_message(self, message_id: UUID, user_id: UUID, is_admin: bool = False) -> Message:
        message = await self.message_repo.get(message_id)
        if not message:
            raise ValueError("Message not found")
        if message.sender_id != user_id and not is_admin:
            raise ValueError("Not authorized to delete this message")
        deleted = await self.message_repo.soft_delete(message_id)
        if not deleted:
            raise ValueError("Message not found")
        logger.info("message_soft_deleted", message_id=str(message_id), user_id=str(user_id), is_admin=is_admin)
        return deleted

    async def _filter_blocked_messages(self, messages: list[Message], user_id: UUID) -> list[Message]:
        blocked_ids = await self.block_repo.get_blocked_ids(user_id)
        blocker_ids = await self.block_repo.get_blocker_ids(user_id)
        excluded = blocked_ids | blocker_ids
        return [
            m for m in messages
            if m.sender_id not in excluded and m.recipient_id not in excluded
        ]
