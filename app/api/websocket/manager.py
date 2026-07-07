from typing import Dict, Set
from uuid import UUID
from app.api.websocket.socketio import socketio


class ConnectionManager:
    def __init__(self):
        self.active_connections: Dict[UUID, str] = {}
        self.user_rooms: Dict[UUID, Set[UUID]] = {}

    async def connect_user(self, user_id: UUID, session_id: str):
        self.active_connections[user_id] = session_id
        await socketio.emit("user_connected", {"user_id": str(user_id)})

    async def disconnect_user(self, user_id: UUID):
        if user_id in self.active_connections:
            del self.active_connections[user_id]
            await socketio.emit("user_disconnected", {"user_id": str(user_id)})

    async def send_to_user(self, user_id: UUID, event: str, data: dict):
        if user_id in self.active_connections:
            session_id = self.active_connections[user_id]
            await socketio.emit(event, data, room=session_id)

    async def send_notification(self, user_id: UUID, event: str, notification_data: dict):
        await self.send_to_user(user_id, event, notification_data)

    async def broadcast_to_chat(self, chat_id: UUID, event: str, data: dict):
        await socketio.emit(event, data, room=f"chat_{chat_id}")

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
