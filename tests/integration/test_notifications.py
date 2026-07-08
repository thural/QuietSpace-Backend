import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_notifications_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/notifications")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_notifications_with_type_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/notifications", params={"type": "LIKE"})
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_unread_count_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/notifications/unread/count")
    assert response.status_code == 403
