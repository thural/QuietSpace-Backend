import pytest
from unittest.mock import AsyncMock, patch, MagicMock
from uuid import UUID, uuid4


@pytest.fixture
def mock_socketio():
    with patch("app.api.websocket.manager.socketio") as mock:
        mock.emit = AsyncMock()
        mock.enter_room = AsyncMock()
        yield mock


@pytest.fixture
def manager(mock_socketio):
    from app.api.websocket.manager import ConnectionManager
    mgr = ConnectionManager()
    mgr.active_connections.clear()
    mgr.user_rooms.clear()
    return mgr


@pytest.mark.asyncio
async def test_connect_user(manager, mock_socketio):
    user_id = uuid4()
    session_id = "test_sid_123"
    await manager.connect_user(user_id, session_id)
    assert manager.active_connections[user_id] == session_id
    mock_socketio.emit.assert_awaited_once()
    args, kwargs = mock_socketio.emit.await_args
    assert args[0] == "user_connected"
    data = args[1]
    assert data["user_id"] == str(user_id)
    assert data["event_type"] == "USER_ONLINE"
    assert "timestamp" in data


@pytest.mark.asyncio
async def test_disconnect_user(manager, mock_socketio):
    user_id = uuid4()
    await manager.connect_user(user_id, "test_sid")
    await manager.disconnect_user(user_id)
    assert user_id not in manager.active_connections
    assert mock_socketio.emit.await_count == 2
    _, disconnect_call = mock_socketio.emit.await_args_list
    args, kwargs = disconnect_call
    assert args[0] == "user_disconnected"
    data = args[1]
    assert data["user_id"] == str(user_id)
    assert data["event_type"] == "USER_OFFLINE"
    assert "timestamp" in data


@pytest.mark.asyncio
async def test_disconnect_nonexistent_user(manager):
    await manager.disconnect_user(uuid4())


@pytest.mark.asyncio
async def test_send_to_user_connected(manager, mock_socketio):
    user_id = uuid4()
    session_id = "test_sid"
    await manager.connect_user(user_id, session_id)
    data = {"msg": "hello"}
    await manager.send_to_user(user_id, "test_event", data)
    mock_socketio.emit.assert_awaited_with("test_event", data, room=session_id)


@pytest.mark.asyncio
async def test_send_to_user_not_connected(manager, mock_socketio):
    await manager.send_to_user(uuid4(), "test_event", {"msg": "hello"})
    mock_socketio.emit.assert_not_awaited()


@pytest.mark.asyncio
async def test_broadcast_to_chat(manager, mock_socketio):
    chat_id = uuid4()
    data = {"msg": "hello"}
    await manager.broadcast_to_chat(chat_id, "chat_event", data)
    mock_socketio.emit.assert_awaited_once_with(
        "chat_event", data, room=f"chat_{chat_id}"
    )


@pytest.mark.asyncio
async def test_join_chat_room(manager, mock_socketio):
    user_id = uuid4()
    chat_id = uuid4()
    session_id = "test_sid"
    await manager.connect_user(user_id, session_id)
    await manager.join_chat_room(user_id, chat_id)
    mock_socketio.enter_room.assert_awaited_once_with(
        session_id, f"chat_{chat_id}"
    )
    assert user_id in manager.user_rooms
    assert chat_id in manager.user_rooms[user_id]


@pytest.mark.asyncio
async def test_join_chat_room_not_connected(manager, mock_socketio):
    await manager.join_chat_room(uuid4(), uuid4())
    mock_socketio.enter_room.assert_not_awaited()


@pytest.mark.asyncio
async def test_is_duplicate_new(manager):
    with patch("app.api.websocket.manager.redis_client") as mock_redis:
        mock_redis.sismember = AsyncMock(return_value=False)
        mock_redis.sadd = AsyncMock()
        mock_redis.expire = AsyncMock()

        result = await manager.is_duplicate("msg_001")
        assert result is False
        mock_redis.sismember.assert_awaited_once_with("ws_dedup", "msg_001")
        mock_redis.sadd.assert_awaited_once_with("ws_dedup", "msg_001")
        mock_redis.expire.assert_awaited_once_with("ws_dedup", 86400)


@pytest.mark.asyncio
async def test_is_duplicate_existing(manager):
    with patch("app.api.websocket.manager.redis_client") as mock_redis:
        mock_redis.sismember = AsyncMock(return_value=True)

        result = await manager.is_duplicate("msg_001")
        assert result is True
        mock_redis.sismember.assert_awaited_once_with("ws_dedup", "msg_001")
        mock_redis.sadd.assert_not_awaited()
        mock_redis.expire.assert_not_awaited()
