from datetime import timedelta
from unittest.mock import AsyncMock, patch
from uuid import uuid4

import pytest
import pytest_asyncio
from httpx import AsyncClient, ASGITransport
from sqlalchemy import select
from sqlalchemy.ext.asyncio import create_async_engine, async_sessionmaker, AsyncSession
from sqlmodel import SQLModel

from app.core.security import create_access_token

TEST_DATABASE_URL = "postgresql+asyncpg://test:test@localhost:5433/test_db"


@pytest_asyncio.fixture
async def shared_engine():
    engine = create_async_engine(TEST_DATABASE_URL, echo=False)
    async with engine.begin() as conn:
        await conn.run_sync(SQLModel.metadata.drop_all)
        await conn.run_sync(SQLModel.metadata.create_all)
    yield engine
    await engine.dispose()


@pytest_asyncio.fixture
async def shared_session_factory(shared_engine):
    return async_sessionmaker(shared_engine, class_=AsyncSession, expire_on_commit=False)


@pytest_asyncio.fixture
def patch_app_session(shared_session_factory):
    from app.main import app

    original = getattr(app.state, "async_session", None)
    app.state.async_session = shared_session_factory
    yield
    if original:
        app.state.async_session = original
    elif hasattr(app.state, "async_session"):
        del app.state.async_session


@pytest_asyncio.fixture
async def ws_test_client(shared_session_factory, patch_app_session):
    from app.main import app
    from app.api.deps import get_db

    async def override_get_db():
        async with shared_session_factory() as session:
            yield session

    app.dependency_overrides[get_db] = override_get_db
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as c:
        yield c
    app.dependency_overrides.clear()


async def _register_and_activate(client: AsyncClient, session_factory, suffix: str):
    email = f"ws-{suffix}-{uuid4().hex[:8]}@test.com"
    username = f"ws{suffix}-{uuid4().hex[:8]}"
    reg = await client.post(
        "/api/v1/auth/register",
        json={"email": email, "username": username, "password": "StrongPass1!", "full_name": f"WS {suffix}"},
    )
    assert reg.status_code == 201

    from app.models.user import User

    async with session_factory() as session:
        result = await session.execute(select(User).where(User.email == email))
        user = result.scalar_one()
        code = user.activation_code

    act = await client.post("/api/v1/auth/activate-account", json={"code": code})
    assert act.status_code == 200

    login = await client.post(
        "/api/v1/auth/login",
        json={"username": username, "password": "StrongPass1!"},
    )
    assert login.status_code == 200
    data = login.json()
    return data["access_token"], data["user"]


@pytest.mark.asyncio
async def test_websocket_auth_with_real_jwt(ws_test_client: AsyncClient, shared_session_factory):
    token, _ = await _register_and_activate(ws_test_client, shared_session_factory, "auth")

    from app.api.websocket.handlers import authenticate_websocket_token

    user = await authenticate_websocket_token(token)
    assert user is not None


@pytest.mark.asyncio
async def test_websocket_auth_with_invalid_jwt(patch_app_session):
    from app.api.websocket.handlers import authenticate_websocket_token

    user = await authenticate_websocket_token("invalid-token")
    assert user is None


@pytest.mark.asyncio
async def test_websocket_auth_with_expired_jwt(patch_app_session):
    from app.api.websocket.handlers import authenticate_websocket_token

    token = create_access_token({"sub": "nobody@test.com"}, expires_delta=timedelta(days=-1))
    user = await authenticate_websocket_token(token)
    assert user is None


@pytest.mark.asyncio
async def test_send_message_via_handler(ws_test_client: AsyncClient, shared_session_factory, patch_app_session):
    from app.api.websocket.handlers import handle_send_message
    from app.api.websocket.manager import manager

    token_a, user_a = await _register_and_activate(ws_test_client, shared_session_factory, "snd1")
    token_b, user_b = await _register_and_activate(ws_test_client, shared_session_factory, "snd2")

    chat_resp = await ws_test_client.post(
        "/api/v1/chats",
        json={
            "participant_ids": [str(user_a["id"]), str(user_b["id"])],
            "is_group": False,
        },
        headers={"Authorization": f"Bearer {token_a}"},
    )
    assert chat_resp.status_code == 201, chat_resp.text
    chat_id = chat_resp.json()["id"]

    manager.active_connections = {}
    with patch.object(manager, "send_to_user", new=AsyncMock()) as mock_send:
        with patch.object(manager, "broadcast_to_chat", new=AsyncMock()) as mock_broadcast:
            await handle_send_message(
                "test-sid",
                {
                    "chat_id": str(chat_id),
                    "sender_id": str(user_a["id"]),
                    "recipient_id": str(user_b["id"]),
                    "text": "Hello via WS!",
                },
            )
            mock_send.assert_awaited()
            mock_broadcast.assert_awaited_once()

    msg_resp = await ws_test_client.get(
        f"/api/v1/messages/chat/{chat_id}",
        headers={"Authorization": f"Bearer {token_a}"},
    )
    assert msg_resp.status_code == 200
    messages = msg_resp.json()
    assert len(messages) >= 1
    assert messages[-1]["text"] == "Hello via WS!"


@pytest.mark.asyncio
async def test_handle_connect_missing_token(ws_test_client, shared_session_factory):
    from app.api.websocket.handlers import handle_connect
    from app.api.websocket.manager import manager
    from unittest.mock import AsyncMock, patch

    with (
        patch.object(manager, "connect_user", new=AsyncMock()) as mock_connect,
        patch.object(manager, "emit_error", new=AsyncMock()) as mock_error,
    ):
        await handle_connect("sid_no_token", {})
        mock_connect.assert_not_awaited()
        mock_error.assert_not_awaited()


@pytest.mark.asyncio
async def test_handle_leave_chat(ws_test_client, shared_session_factory, patch_app_session):
    from app.api.websocket.handlers import handle_leave_chat
    from app.api.websocket.manager import manager
    from unittest.mock import AsyncMock, patch

    token_a, user_a = await _register_and_activate(ws_test_client, shared_session_factory, "leave1")
    token_b, user_b = await _register_and_activate(ws_test_client, shared_session_factory, "leave2")

    chat_resp = await ws_test_client.post(
        "/api/v1/chats",
        json={
            "participant_ids": [str(user_a["id"]), str(user_b["id"])],
            "is_group": False,
        },
        headers={"Authorization": f"Bearer {token_a}"},
    )
    assert chat_resp.status_code == 201
    chat_id = chat_resp.json()["id"]

    with patch.object(manager, "broadcast_to_chat", new=AsyncMock()) as mock_broadcast:
        await handle_leave_chat(
            "test-sid",
            {
                "user_id": str(user_a["id"]),
                "chat_id": str(chat_id),
            },
        )
        mock_broadcast.assert_awaited_once()


@pytest.mark.asyncio
async def test_handle_online_status_via_handler(ws_test_client, shared_session_factory, patch_app_session):
    from app.api.websocket.handlers import handle_online_status
    from app.api.websocket.manager import manager
    from unittest.mock import AsyncMock, patch

    token_a, user_a = await _register_and_activate(ws_test_client, shared_session_factory, "onl1")

    with patch.object(manager, "broadcast_to_chat", new=AsyncMock()) as mock_broadcast:
        await handle_online_status(
            "test-sid",
            {
                "user_id": str(user_a["id"]),
                "status": "online",
            },
        )
        mock_broadcast.assert_awaited_once()
