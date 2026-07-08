import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_create_chat_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/chats",
        json={"name": "Test Chat", "is_group": False},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_chats_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/chats")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_chat_not_found(client: AsyncClient):
    response = await client.get("/api/v1/chats/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_chat_messages_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/chats/00000000-0000-0000-0000-000000000000/messages")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_send_chat_message_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/chats/00000000-0000-0000-0000-000000000000/messages",
        json={"chat_id": "00000000-0000-0000-0000-000000000000", "recipient_id": "00000000-0000-0000-0000-000000000001", "text": "Hello"},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_delete_chat_requires_auth(client: AsyncClient):
    response = await client.delete("/api/v1/chats/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_update_chat_not_member(client: AsyncClient, db_session):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User
    from app.models.chat import Chat

    user = User(username="chatupdater", email="chatupdater@test.com", password_hash="x")
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)

    chat = Chat(name="Test Chat")
    db_session.add(chat)
    await db_session.commit()

    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.patch(
        f"/api/v1/chats/{chat.id}",
        json={"name": "Updated Name"},
    )
    assert response.status_code == 403

    del app.dependency_overrides[get_current_user]


@pytest.mark.asyncio
async def test_add_participant_not_member(client: AsyncClient, db_session):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User
    from app.models.chat import Chat

    user = User(username="addpart", email="addpart@test.com", password_hash="x")
    other = User(username="otherpart", email="otherpart@test.com", password_hash="x")
    db_session.add_all([user, other])
    await db_session.commit()
    await db_session.refresh(user)
    await db_session.refresh(other)

    chat = Chat(name="Group Chat")
    db_session.add(chat)
    await db_session.commit()

    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.post(
        f"/api/v1/chats/{chat.id}/participants",
        params={"user_id": str(other.id)},
    )
    assert response.status_code == 403

    del app.dependency_overrides[get_current_user]
