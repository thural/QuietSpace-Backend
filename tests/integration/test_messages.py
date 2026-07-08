import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_send_message_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/messages",
        json={
            "chat_id": "00000000-0000-0000-0000-000000000000",
            "recipient_id": "00000000-0000-0000-0000-000000000000",
            "text": "Hello!",
        },
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_chat_messages_requires_auth(client: AsyncClient):
    response = await client.get(
        "/api/v1/messages/chat/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_message_not_found(client: AsyncClient):
    response = await client.get(
        "/api/v1/messages/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_get_unread_messages_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/messages/unread")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_mark_message_read_not_found(client: AsyncClient):
    response = await client.put(
        "/api/v1/messages/00000000-0000-0000-0000-000000000000/read"
    )
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_delete_message_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/messages/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403
