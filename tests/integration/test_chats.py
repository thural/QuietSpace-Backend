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
