from datetime import datetime, timezone
from typing import Dict, Set
from uuid import UUID
from app.api.websocket.socketio import socketio
from app.config.redis import redis_client
from app.enums.websocket_event_type import ErrorCode

ONLINE_USERS_KEY = "online_users"


class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[UUID, str] = {}
        self.sid_to_user: Dict[str, UUID] = {}
        self.user_rooms: Dict[UUID, Set[UUID]] = {}

    DEDUP_KEY = "ws_dedup"
    DEDUP_TTL = 86400

    async def is_duplicate(self, client_message_id: str) -> bool:
        exists = await redis_client.sismember(self.DEDUP_KEY, client_message_id)
        if not exists:
            await redis_client.sadd(self.DEDUP_KEY, client_message_id)
            await redis_client.expire(self.DEDUP_KEY, self.DEDUP_TTL)
        return bool(exists)

    async def emit_error(self, sid: str, code: ErrorCode, message: str, operation: str):
        await socketio.emit(
            "error",
            {
                "event_type": "error",
                "code": code.value,
                "message": message,
                "operation": operation,
                "timestamp": datetime.now(timezone.utc).isoformat(),
            },
            to=sid,
        )

    async def connect_user(self, user_id: UUID, session_id: str):
        self.active_connections[user_id] = session_id
        self.sid_to_user[session_id] = user_id
        await redis_client.sadd(ONLINE_USERS_KEY, str(user_id))
        now = datetime.now(timezone.utc).isoformat()
        await socketio.emit("user_connected", {
            "event_type": "USER_ONLINE",
            "user_id": str(user_id),
            "timestamp": now,
        })

    async def disconnect_user(self, user_id: UUID):
        if user_id in self.active_connections:
            session_id = self.active_connections.pop(user_id)
            self.sid_to_user.pop(session_id, None)
            await redis_client.srem(ONLINE_USERS_KEY, str(user_id))
            now = datetime.now(timezone.utc).isoformat()
            await socketio.emit("user_disconnected", {
                "event_type": "USER_OFFLINE",
                "user_id": str(user_id),
                "timestamp": now,
            })

    async def get_online_users(self) -> list[UUID]:
        user_ids = await redis_client.smembers(ONLINE_USERS_KEY)
        return [UUID(uid) for uid in user_ids]

    async def send_to_user(self, user_id: UUID, event: str, data: dict):
        if user_id in self.active_connections:
            session_id = self.active_connections[user_id]
            await socketio.emit(event, data, room=session_id)

    async def send_notification(self, user_id: UUID, event: str, notification_data: dict):
        await self.send_to_user(user_id, event, notification_data)

    async def broadcast_to_chat(self, chat_id: UUID, event: str, data: dict):
        await socketio.emit(event, data, room=f"chat_{chat_id}")

    async def broadcast_to_public(self, event: str, data: dict):
        await socketio.emit(event, data, room="public")

    async def join_chat_room(self, user_id: UUID, chat_id: UUID):
        session_id = self.active_connections.get(user_id)
        if session_id:
            await socketio.enter_room(session_id, f"chat_{chat_id}")
            if user_id not in self.user_rooms:
                self.user_rooms[user_id] = set()
            self.user_rooms[user_id].add(chat_id)

    async def leave_chat_room(self, user_id: UUID, chat_id: UUID):
        session_id = self.active_connections.get(user_id)
        if session_id:
            await socketio.leave_room(session_id, f"chat_{chat_id}")
            if user_id in self.user_rooms:
                self.user_rooms[user_id].discard(chat_id)

    def is_user_in_chat(self, user_id: UUID, chat_id: UUID) -> bool:
        return user_id in self.user_rooms and chat_id in self.user_rooms[user_id]


manager = ConnectionManager()
