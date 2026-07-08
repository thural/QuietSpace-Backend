import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_admin_list_users_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/admin/users")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_admin_disable_user_requires_auth(client: AsyncClient):
    response = await client.put(
        "/api/v1/admin/users/00000000-0000-0000-0000-000000000000/disable"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_admin_delete_user_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/admin/users/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_admin_list_users_success(client: AsyncClient, db_session):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User
    from app.enums.role import Role

    admin = User(username="adminuser", email="admin@test.com", password_hash="x", role=Role.ADMIN)
    db_session.add(admin)
    await db_session.commit()
    await db_session.refresh(admin)

    app.dependency_overrides[get_current_user] = lambda: admin
    response = await client.get("/api/v1/admin/users")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data

    del app.dependency_overrides[get_current_user]


@pytest.mark.asyncio
async def test_admin_disable_user_success(client: AsyncClient, db_session):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User
    from app.enums.role import Role

    admin = User(username="admin2", email="admin2@test.com", password_hash="x", role=Role.ADMIN)
    target = User(username="target", email="target@test.com", password_hash="x")
    db_session.add_all([admin, target])
    await db_session.commit()
    await db_session.refresh(target)

    app.dependency_overrides[get_current_user] = lambda: admin
    response = await client.put(f"/api/v1/admin/users/{target.id}/disable")
    assert response.status_code == 200

    del app.dependency_overrides[get_current_user]
