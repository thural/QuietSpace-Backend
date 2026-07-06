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
