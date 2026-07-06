from app.api.websocket.socketio import socketio
from app.api.websocket.manager import manager


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


@socketio.on("send_message")
async def handle_send_message(sid, data):
    pass


@socketio.on("set_online_status")
async def handle_online_status(sid, data):
    user_id = UUID(data["user_id"])
    status = data["status"]
    await manager.broadcast_to_chat(user_id, "user_status", {"user_id": str(user_id), "status": status})


async def authenticate_websocket_token(token: str):
    try:
        from app.core.security import decode_token
        payload = decode_token(token)
        from app.repositories.user import user_repository
        return await user_repository.get_by_email(payload.get("sub"))
    except Exception:
        return None
