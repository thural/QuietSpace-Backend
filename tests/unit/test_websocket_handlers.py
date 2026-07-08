import pytest
from unittest.mock import AsyncMock, patch, MagicMock
from uuid import UUID, uuid4


@pytest.fixture(autouse=True)
def mock_socketio():
    with patch("app.api.websocket.handlers.socketio") as mock:
        mock.emit = AsyncMock()
        mock.enter_room = AsyncMock()
        yield mock


@pytest.fixture(autouse=True)
def mock_rate_limiter():
    with patch("app.api.websocket.handlers.rate_limiter") as mock:
        mock.check = AsyncMock(return_value=True)
        yield mock


@pytest.fixture(autouse=True)
def mock_manager():
    with patch("app.api.websocket.handlers.manager") as mock:
        mock.connect_user = AsyncMock()
        mock.disconnect_user = AsyncMock()
        mock.send_to_user = AsyncMock()
        mock.broadcast_to_chat = AsyncMock()
        mock.join_chat_room = AsyncMock()
        mock.leave_chat_room = AsyncMock()
        mock.emit_error = AsyncMock()
        mock.is_duplicate = AsyncMock(return_value=False)
        mock.should_throttle_typing = MagicMock(return_value=False)
        mock.active_connections = {uuid4(): "sid_1", uuid4(): "sid_2"}
        yield mock


@pytest.mark.asyncio
async def test_handle_connect_with_valid_token(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_connect

    mock_user = MagicMock()
    mock_user.id = uuid4()
    with patch(
        "app.api.websocket.handlers.authenticate_websocket_token",
        AsyncMock(return_value=mock_user),
    ):
        environ = {"HTTP_AUTHORIZATION": "Bearer valid_token"}
        await handle_connect("sid_123", environ)
        mock_manager.connect_user.assert_awaited_once_with(mock_user.id, "sid_123")
        mock_socketio.emit.assert_awaited_once_with(
            "connected", {"user_id": str(mock_user.id)}, to="sid_123"
        )


@pytest.mark.asyncio
async def test_handle_connect_without_token(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_connect

    environ = {}
    await handle_connect("sid_123", environ)
    mock_manager.connect_user.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_connect_with_invalid_token(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_connect
    from app.enums.websocket_event_type import ErrorCode

    with patch(
        "app.api.websocket.handlers.authenticate_websocket_token",
        AsyncMock(return_value=None),
    ):
        environ = {"HTTP_AUTHORIZATION": "Bearer invalid_token"}
        await handle_connect("sid_123", environ)
        mock_manager.connect_user.assert_not_awaited()
        mock_manager.emit_error.assert_awaited_once_with(
            "sid_123", ErrorCode.AUTH_FAILED, "Invalid or expired token", "connect"
        )


@pytest.mark.asyncio
async def test_handle_disconnect(mock_manager):
    from app.api.websocket.handlers import handle_disconnect

    mock_manager.active_connections = {uuid4(): "sid_123"}
    await handle_disconnect("sid_123")
    user_id = list(mock_manager.active_connections.keys())[0]
    mock_manager.disconnect_user.assert_awaited_once_with(user_id)


@pytest.mark.asyncio
async def test_handle_disconnect_unknown_sid(mock_manager):
    from app.api.websocket.handlers import handle_disconnect

    mock_manager.active_connections = {uuid4(): "sid_1"}
    await handle_disconnect("unknown_sid")
    mock_manager.disconnect_user.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_join_chat(mock_manager):
    from app.api.websocket.handlers import handle_join_chat

    user_id = uuid4()
    chat_id = uuid4()
    data = {"user_id": str(user_id), "chat_id": str(chat_id)}

    mock_chat = MagicMock()
    mock_chat.id = chat_id

    mock_repo = MagicMock()
    mock_repo.get = AsyncMock(return_value=mock_chat)

    mock_participant = MagicMock()
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = mock_participant

    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_session.execute = AsyncMock(return_value=mock_result)

    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    with (
        patch("app.main.app", mock_app),
        patch("app.repositories.chat.ChatRepository", return_value=mock_repo),
    ):
        await handle_join_chat("sid_123", data)
        mock_manager.join_chat_room.assert_awaited_once_with(user_id, chat_id)


@pytest.mark.asyncio
async def test_handle_send_message(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_send_message

    chat_id = uuid4()
    sender_id = uuid4()
    recipient_id = uuid4()
    data = {
        "chat_id": str(chat_id),
        "sender_id": str(sender_id),
        "recipient_id": str(recipient_id),
        "text": "Hello!",
    }

    mock_saved_message = MagicMock()
    mock_saved_message.sender_id = sender_id
    mock_saved_message.recipient_id = recipient_id
    mock_saved_message.chat_id = chat_id
    mock_saved_message.model_dump.return_value = {"id": "msg_1", "text": "Hello!"}

    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session

    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    mock_service = MagicMock()
    mock_service.add_message = AsyncMock(return_value=mock_saved_message)

    mock_chat_event = MagicMock()
    expected_envelope = {
        "event_type": "NEW_MESSAGE",
        "timestamp": "2025-01-01T00:00:00",
        "actor_id": str(sender_id),
        "data": {"id": "msg_1", "text": "Hello!"},
        "chat_id": str(chat_id),
        "message_id": None,
        "recipient_id": None,
    }
    mock_chat_event.model_dump.return_value = expected_envelope

    with (
        patch("app.main.app", mock_app),
        patch("app.services.message_service.MessageService", return_value=mock_service),
        patch("app.api.websocket.handlers.EventFactory.create_chat_event", return_value=mock_chat_event),
    ):
        await handle_send_message("sid_123", data)

        mock_service.add_message.assert_awaited_once_with({
            "chat_id": chat_id,
            "sender_id": sender_id,
            "recipient_id": recipient_id,
            "text": "Hello!",
        })
        assert mock_manager.send_to_user.await_count == 2
        mock_manager.send_to_user.assert_any_await(
            sender_id, "new_message", expected_envelope
        )
        mock_manager.send_to_user.assert_any_await(
            recipient_id, "new_message", expected_envelope
        )
        mock_manager.broadcast_to_chat.assert_awaited_once_with(
            chat_id, "message_in_chat", expected_envelope
        )
        mock_session.commit.assert_awaited_once()


@pytest.mark.asyncio
async def test_handle_send_message_dedup(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_send_message

    mock_manager.is_duplicate = AsyncMock(return_value=True)
    data = {
        "chat_id": str(uuid4()),
        "sender_id": str(uuid4()),
        "recipient_id": str(uuid4()),
        "text": "Hello!",
        "client_message_id": "dup_001",
    }
    await handle_send_message("sid_123", data)
    mock_manager.is_duplicate.assert_awaited_once_with("dup_001")
    mock_manager.send_to_user.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_online_status(mock_manager):
    from app.api.websocket.handlers import handle_online_status

    user_id = uuid4()
    data = {"user_id": str(user_id), "status": "online"}
    await handle_online_status("sid_123", data)
    mock_manager.broadcast_to_chat.assert_awaited_once_with(
        user_id, "user_status", {"user_id": str(user_id), "status": "online"}
    )


@pytest.mark.asyncio
async def test_handle_typing_status(mock_manager):
    from app.api.websocket.handlers import handle_typing_status

    user_id = uuid4()
    chat_id = uuid4()
    mock_manager.should_throttle_typing = MagicMock(return_value=False)
    data = {"user_id": str(user_id), "chat_id": str(chat_id)}
    await handle_typing_status("sid_123", data)
    mock_manager.broadcast_to_chat.assert_awaited_once_with(
        chat_id, "typing_status", {"user_id": str(user_id), "chat_id": str(chat_id)}
    )


@pytest.mark.asyncio
async def test_handle_typing_status_throttled(mock_manager):
    from app.api.websocket.handlers import handle_typing_status

    mock_manager.should_throttle_typing = MagicMock(return_value=True)
    data = {"user_id": str(uuid4()), "chat_id": str(uuid4())}
    await handle_typing_status("sid_123", data)
    mock_manager.broadcast_to_chat.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_send_message_rate_limited(mock_manager, mock_rate_limiter):
    from app.api.websocket.handlers import handle_send_message

    mock_rate_limiter.check = AsyncMock(return_value=False)
    data = {
        "chat_id": str(uuid4()),
        "sender_id": str(uuid4()),
        "recipient_id": str(uuid4()),
        "text": "Hello!",
    }
    await handle_send_message("sid_123", data)
    mock_manager.emit_error.assert_awaited_once()
    mock_manager.send_to_user.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_join_chat_not_found(mock_manager):
    from app.api.websocket.handlers import handle_join_chat

    mock_repo = MagicMock()
    mock_repo.get = AsyncMock(return_value=None)
    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    data = {"user_id": str(uuid4()), "chat_id": str(uuid4())}
    with (
        patch("app.main.app", mock_app),
        patch("app.repositories.chat.ChatRepository", return_value=mock_repo),
    ):
        await handle_join_chat("sid_123", data)
        mock_manager.join_chat_room.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_join_chat_not_member(mock_manager):
    from app.api.websocket.handlers import handle_join_chat

    mock_chat = MagicMock()
    mock_chat.id = uuid4()
    mock_repo = MagicMock()
    mock_repo.get = AsyncMock(return_value=mock_chat)
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_session.execute = AsyncMock(return_value=mock_result)
    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    data = {"user_id": str(uuid4()), "chat_id": str(mock_chat.id)}
    with (
        patch("app.main.app", mock_app),
        patch("app.repositories.chat.ChatRepository", return_value=mock_repo),
    ):
        await handle_join_chat("sid_123", data)
        mock_manager.join_chat_room.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_leave_chat(mock_manager):
    from app.api.websocket.handlers import handle_leave_chat

    user_id = uuid4()
    chat_id = uuid4()
    data = {"user_id": str(user_id), "chat_id": str(chat_id)}
    await handle_leave_chat("sid_123", data)
    mock_manager.leave_chat_room.assert_awaited_once_with(user_id, chat_id)
    mock_manager.broadcast_to_chat.assert_awaited_once()


@pytest.mark.asyncio
async def test_authenticate_websocket_token_success():
    from app.api.websocket.handlers import authenticate_websocket_token

    mock_user = MagicMock()
    mock_repo = MagicMock()
    mock_repo.get_by_email = AsyncMock(return_value=mock_user)
    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    with (
        patch("app.core.security.decode_token", return_value={"sub": "test@test.com"}),
        patch("app.main.app", mock_app),
        patch("app.repositories.user.UserRepository", return_value=mock_repo),
    ):
        result = await authenticate_websocket_token("valid_token")
        assert result is mock_user
        mock_repo.get_by_email.assert_awaited_once_with("test@test.com")


@pytest.mark.asyncio
async def test_handle_delete_message_not_authorized(mock_manager):
    from app.api.websocket.handlers import handle_delete_message

    data = {
        "message_id": str(uuid4()),
        "user_id": str(uuid4()),
        "chat_id": str(uuid4()),
    }
    mock_service = MagicMock()
    mock_service.delete_message = AsyncMock(side_effect=ValueError("Not authorized"))
    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    with (
        patch("app.main.app", mock_app),
        patch("app.services.message_service.MessageService", return_value=mock_service),
    ):
        await handle_delete_message("sid_123", data)
        mock_manager.emit_error.assert_awaited_once()


@pytest.mark.asyncio
async def test_handle_seen_message_not_found(mock_manager):
    from app.api.websocket.handlers import handle_seen_message

    data = {
        "message_id": str(uuid4()),
        "user_id": str(uuid4()),
        "chat_id": str(uuid4()),
    }
    mock_service = MagicMock()
    mock_service.mark_as_read = AsyncMock(return_value=None)
    mock_session = AsyncMock()
    mock_session.__aenter__.return_value = mock_session
    mock_app = MagicMock()
    mock_app.state.async_session.return_value = mock_session

    with (
        patch("app.main.app", mock_app),
        patch("app.services.message_service.MessageService", return_value=mock_service),
    ):
        await handle_seen_message("sid_123", data)
        mock_manager.emit_error.assert_awaited_once()


@pytest.mark.asyncio
async def test_handle_get_online_users_not_authenticated(mock_manager, mock_socketio):
    from app.api.websocket.handlers import handle_get_online_users

    mock_manager.sid_to_user = {}
    data = {}
    await handle_get_online_users("sid_123", data)
    mock_socketio.emit.assert_awaited_once_with(
        "online_users", {"online_users": []}, to="sid_123"
    )


@pytest.mark.asyncio
async def test_handle_public_message_rate_limited(mock_manager, mock_rate_limiter):
    from app.api.websocket.handlers import handle_public_message

    mock_rate_limiter.check = AsyncMock(return_value=False)
    data = {"user_id": str(uuid4()), "message": "Hello", "type": "message"}
    await handle_public_message("sid_123", data)
    mock_manager.emit_error.assert_awaited_once()


@pytest.mark.asyncio
async def test_handle_online_status_rate_limited(mock_manager, mock_rate_limiter):
    from app.api.websocket.handlers import handle_online_status

    mock_rate_limiter.check = AsyncMock(return_value=False)
    data = {"user_id": str(uuid4()), "status": "online"}
    await handle_online_status("sid_123", data)
    mock_manager.broadcast_to_chat.assert_not_awaited()


@pytest.mark.asyncio
async def test_authenticate_websocket_token_failure():
    from app.api.websocket.handlers import authenticate_websocket_token

    with patch("app.core.security.decode_token", side_effect=Exception("bad token")):
        result = await authenticate_websocket_token("bad_token")
        assert result is None
