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


@pytest.mark.asyncio
async def test_batch_read_notifications_by_ids(client: AsyncClient, db_session):
    from app.api.deps import get_current_user
    from app.models.user import User
    from app.models.notification import Notification

    user = User(username="notifuser2", email="notifuser2@test.com", password_hash="x")
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)

    notif1 = Notification(user_id=user.id, type="LIKE", title="Like 1", content="You got a like")
    notif2 = Notification(user_id=user.id, type="LIKE", title="Like 2", content="You got another like")
    db_session.add(notif1)
    db_session.add(notif2)
    await db_session.commit()
    await db_session.refresh(notif1)
    await db_session.refresh(notif2)

    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.put(
        "/api/v1/notifications/read",
        json={"ids": [str(notif1.id), str(notif2.id)]},
    )
    assert response.status_code == 200
    data = response.json()
    assert data["marked"] == 2
    del app.dependency_overrides[get_current_user]
