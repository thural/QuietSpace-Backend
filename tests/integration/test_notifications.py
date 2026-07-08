import pytest
from httpx import AsyncClient
from app.main import app


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


@pytest.mark.asyncio
async def test_batch_read_notifications_requires_auth(client: AsyncClient):
    response = await client.put("/api/v1/notifications/read", json={"all": True})
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_batch_read_notifications_all(client: AsyncClient, db_session):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User

    user = User(username="notifuser", email="notifuser@test.com", password_hash="x")
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)

    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.put("/api/v1/notifications/read", json={"all": True})
    assert response.status_code == 200
    data = response.json()
    assert "marked" in data
    del app.dependency_overrides[get_current_user]
