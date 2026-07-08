from datetime import datetime, timezone
from uuid import UUID
import structlog
from app.api.websocket.socketio import socketio
from app.api.websocket.manager import manager
from app.models.websocket_event import EventFactory
from app.enums.websocket_event_type import WebSocketEventType, ErrorCode
from app.api.websocket.rate_limiter import rate_limiter

logger = structlog.get_logger()


@socketio.on("connect")
async def handle_connect(sid, environ):
    auth_token = environ.get("HTTP_AUTHORIZATION", "").replace("Bearer ", "")
    if not auth_token:
        logger.warning("ws_connect_no_token", sid=sid)
        return
    user = await authenticate_websocket_token(auth_token)
    if user:
        await manager.connect_user(user.id, sid)
        await socketio.enter_room(sid, "public")
        await socketio.emit("connected", {"user_id": str(user.id)}, to=sid)
        logger.info("ws_connected", user_id=str(user.id), sid=sid)
    else:
        await manager.emit_error(sid, ErrorCode.AUTH_FAILED, "Invalid or expired token", "connect")


@socketio.on("disconnect")
async def handle_disconnect(sid):
    for user_id, session_id in list(manager.active_connections.items()):
        if session_id == sid:
            await manager.disconnect_user(user_id)
            logger.info("ws_disconnected", user_id=str(user_id), sid=sid)
            break


@socketio.on("join_chat")
async def handle_join_chat(sid, data):
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])
    from app.main import app
    from app.repositories.chat import ChatRepository
    async with app.state.async_session() as session:
        repo = ChatRepository(session)
        chat = await repo.get(chat_id)
        if not chat:
            await manager.emit_error(sid, ErrorCode.NOT_FOUND, "Chat not found", "join_chat")
            return
        from app.models.chat_participant import ChatParticipant
        from sqlalchemy import select
        result = await session.execute(
            select(ChatParticipant).where(
                ChatParticipant.chat_id == chat_id,
                ChatParticipant.user_id == user_id,
            )
        )
        if not result.scalar_one_or_none():
            await manager.emit_error(sid, ErrorCode.FORBIDDEN, "Not a chat member", "join_chat")
            return
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
    client_message_id = data.get("client_message_id")
    if client_message_id and await manager.is_duplicate(client_message_id):
        logger.warning("ws_duplicate_message", client_message_id=client_message_id)
        return

    sender_id = UUID(data["sender_id"])
    client_ts = data.get("client_timestamp")
    if not await rate_limiter.check(sender_id, "send_message", 10, 10):
        await manager.emit_error(sid, ErrorCode.RATE_LIMITED, "Too many messages", "send_message")
        return

    from app.main import app
    from app.services.message_service import MessageService

    async with app.state.async_session() as session:
        message_data = {
            "chat_id": UUID(data["chat_id"]),
            "sender_id": sender_id,
            "recipient_id": UUID(data["recipient_id"]),
            "text": data["text"],
        }
        service = MessageService(session)
        saved_message = await service.add_message(message_data)

        raw_data = saved_message.model_dump(mode="json")
        if client_ts:
            raw_data["client_timestamp"] = client_ts
            raw_data["server_timestamp"] = datetime.now(timezone.utc).isoformat()
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.NEW_MESSAGE,
            actor_id=saved_message.sender_id,
            chat_id=saved_message.chat_id,
            data=raw_data,
        )
        envelope = event.model_dump(mode="json")

        await manager.send_to_user(
            saved_message.sender_id, "new_message", envelope
        )
        await manager.send_to_user(
            saved_message.recipient_id, "new_message", envelope
        )
        await manager.broadcast_to_chat(
            saved_message.chat_id, "message_in_chat", envelope
        )
        await session.commit()
        logger.info("ws_message_sent", message_id=str(saved_message.id), chat_id=str(saved_message.chat_id), sender_id=str(saved_message.sender_id))


@socketio.on("set_online_status")
async def handle_online_status(sid, data):
    user_id = UUID(data["user_id"])
    if not await rate_limiter.check(user_id, "set_online_status", 5, 60):
        return
    status = data["status"]
    await manager.broadcast_to_chat(user_id, "user_status", {"user_id": str(user_id), "status": status})


@socketio.on("typing_status")
async def handle_typing_status(sid, data):
    user_id = UUID(data["user_id"])
    chat_id = UUID(data["chat_id"])
    if manager.should_throttle_typing(user_id, chat_id):
        return
    await manager.broadcast_to_chat(
        chat_id, "typing_status", {"user_id": str(user_id), "chat_id": str(chat_id)}
    )


@socketio.on("get_online_users")
async def handle_get_online_users(sid, data):
    user_id = manager.sid_to_user.get(sid)
    if not user_id:
        await socketio.emit("online_users", {"online_users": []}, to=sid)
        return
    from app.main import app
    from app.repositories.user import UserRepository
    async with app.state.async_session() as session:
        repo = UserRepository(session)
        following_ids = await repo.get_following_ids(user_id)
    online_users = await manager.get_online_users()
    scoped = [uid for uid in online_users if uid in following_ids]
    await socketio.emit("online_users", {"online_users": [str(uid) for uid in scoped]}, to=sid)


@socketio.on("public_message")
async def handle_public_message(sid, data):
    user_id_raw = data.get("user_id")
    user_uuid = None
    if user_id_raw:
        try:
            user_uuid = UUID(user_id_raw)
            if not await rate_limiter.check(user_uuid, "public_message", 5, 10):
                await manager.emit_error(sid, ErrorCode.RATE_LIMITED, "Too many public messages", "public_message")
                return
        except ValueError:
            pass
    message = data.get("message", "")
    if user_uuid:
        from app.main import app
        from app.models.message import Message
        from app.repositories.chat import ChatRepository
        async with app.state.async_session() as session:
            chat_repo = ChatRepository(session)
            public_chat = await chat_repo.get_or_create_public_chat()
            msg = Message(
                chat_id=public_chat.id,
                sender_id=user_uuid,
                recipient_id=user_uuid,
                text=message,
            )
            session.add(msg)
            await session.commit()
    await manager.broadcast_to_public(
        "public_message",
        {
            "user_id": user_id_raw,
            "message": message,
            "type": data.get("type", "message"),
        },
    )


@socketio.on("delete_message")
async def handle_delete_message(sid, data):
    client_message_id = data.get("client_message_id")
    if client_message_id and await manager.is_duplicate(client_message_id):
        logger.warning("ws_duplicate_message", client_message_id=client_message_id)
        return

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
            await manager.emit_error(sid, ErrorCode.FORBIDDEN, "Not authorized to delete this message", "delete_message")
            return
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.DELETE_MESSAGE,
            actor_id=user_id,
            chat_id=chat_id,
            message_id=message_id,
        )
        await manager.broadcast_to_chat(chat_id, "chat_event", event.model_dump(mode="json"))
        logger.info("ws_message_deleted", message_id=str(message_id), chat_id=str(chat_id), user_id=str(user_id))


@socketio.on("seen_message")
async def handle_seen_message(sid, data):
    client_message_id = data.get("client_message_id")
    if client_message_id and await manager.is_duplicate(client_message_id):
        logger.warning("ws_duplicate_message", client_message_id=client_message_id)
        return

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
        else:
            await manager.emit_error(sid, ErrorCode.NOT_FOUND, "Message not found", "seen_message")


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
