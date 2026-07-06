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
