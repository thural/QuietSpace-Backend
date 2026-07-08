import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_me_unauthenticated(client: AsyncClient):
    response = await client.get("/api/v1/users/me")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_search_users(client: AsyncClient):
    response = await client.get("/api/v1/users/search", params={"q": "test"})
    assert response.status_code == 200
    assert isinstance(response.json(), list)


@pytest.mark.asyncio
async def test_get_user_not_found(client: AsyncClient):
    response = await client.get("/api/v1/users/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_remove_follower_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/users/00000000-0000-0000-0000-000000000000/followers/00000000-0000-0000-0000-000000000001"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_remove_follower_deprecated_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/users/followers/remove/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_block_user_me_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/users/me/block/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_unblock_user_me_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/users/me/block/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_blocked_users_me_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/users/me/blocked")
    assert response.status_code == 403
