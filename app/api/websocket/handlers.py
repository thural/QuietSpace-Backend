from uuid import UUID
from app.api.websocket.socketio import socketio
from app.api.websocket.manager import manager
from app.models.websocket_event import EventFactory
from app.enums.websocket_event_type import WebSocketEventType


@socketio.on("connect")
async def handle_connect(sid, environ):
    auth_token = environ.get("HTTP_AUTHORIZATION", "").replace("Bearer ", "")
    if not auth_token:
        return
    user = await authenticate_websocket_token(auth_token)
    if user:
        await manager.connect_user(user.id, sid)
        await socketio.emit("connected", {"user_id": str(user.id)}, to=sid)


@socketio.on("disconnect")
async def handle_disconnect(sid):
    for user_id, session_id in list(manager.active_connections.items()):
        if session_id == sid:
            await manager.disconnect_user(user_id)
            break


@socketio.on("join_chat")
async def handle_join_chat(sid, data):
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])
    await manager.join_chat_room(user_id, chat_id)
    event = EventFactory.create_chat_event(
        event_type=WebSocketEventType.JOIN_CHAT,
        actor_id=user_id,
        chat_id=chat_id,
    )
    await manager.broadcast_to_chat(chat_id, "chat_event", event.model_dump(mode="json"))


@socketio.on("leave_chat")
async def handle_leave_chat(sid, data):
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])
    await manager.leave_chat_room(user_id, chat_id)
    event = EventFactory.create_chat_event(
        event_type=WebSocketEventType.LEAVE_CHAT,
        actor_id=user_id,
        chat_id=chat_id,
    )
    await manager.broadcast_to_chat(chat_id, "chat_event", event.model_dump(mode="json"))


@socketio.on("send_message")
async def handle_send_message(sid, data):
    from app.main import app
    from app.services.message_service import MessageService

    async with app.state.async_session() as session:
        message_data = {
            "chat_id": UUID(data["chat_id"]),
            "sender_id": UUID(data["sender_id"]),
            "recipient_id": UUID(data["recipient_id"]),
            "text": data["text"],
        }
        service = MessageService(session)
        saved_message = await service.add_message(message_data)

        await manager.send_to_user(
            saved_message.sender_id, "new_message", saved_message.model_dump(mode="json")
        )
        await manager.send_to_user(
            saved_message.recipient_id, "new_message", saved_message.model_dump(mode="json")
        )
        await manager.broadcast_to_chat(
            saved_message.chat_id, "message_in_chat", saved_message.model_dump(mode="json")
        )
        await session.commit()


@socketio.on("set_online_status")
async def handle_online_status(sid, data):
    user_id = UUID(data["user_id"])
    status = data["status"]
    await manager.broadcast_to_chat(user_id, "user_status", {"user_id": str(user_id), "status": status})


@socketio.on("delete_message")
async def handle_delete_message(sid, data):
    from app.main import app
    from app.services.message_service import MessageService

    message_id = UUID(data["message_id"])
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])

    async with app.state.async_session() as session:
        service = MessageService(session)
        try:
            await service.delete_message(message_id, user_id)
        except ValueError:
            return
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.DELETE_MESSAGE,
            actor_id=user_id,
            chat_id=chat_id,
            message_id=message_id,
        )
        await manager.broadcast_to_chat(chat_id, "chat_event", event.model_dump(mode="json"))


@socketio.on("seen_message")
async def handle_seen_message(sid, data):
    from app.main import app
    from app.services.message_service import MessageService

    message_id = UUID(data["message_id"])
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])

    async with app.state.async_session() as session:
        service = MessageService(session)
        result = await service.mark_as_read(message_id, user_id)
        if result:
            await session.commit()
            sender_id, _ = result
            event = EventFactory.create_chat_event(
                event_type=WebSocketEventType.SEEN_MESSAGE,
                actor_id=user_id,
                chat_id=chat_id,
                message_id=message_id,
                recipient_id=sender_id,
                data={"read_by": str(user_id)},
            )
            await manager.broadcast_to_chat(chat_id, "chat_event", event.model_dump(mode="json"))


async def authenticate_websocket_token(token: str):
    try:
        from app.core.security import decode_token
        payload = decode_token(token)
        from app.main import app
        from app.repositories.user import UserRepository
        async with app.state.async_session() as session:
            repo = UserRepository(session)
            return await repo.get_by_email(payload.get("sub"))
    except Exception:
        return None
