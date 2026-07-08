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
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


@pytest.mark.asyncio
async def test_get_user_not_found(client: AsyncClient):
    response = await client.get("/api/v1/users/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_get_user_detail_not_found(client: AsyncClient):
    response = await client.get("/api/v1/users/00000000-0000-0000-0000-000000000000/detail")
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


@pytest.mark.asyncio
async def test_delete_user_requires_auth(client: AsyncClient):
    response = await client.delete("/api/v1/users/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_online_users(client: AsyncClient):
    response = await client.get("/api/v1/users/online")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)


@pytest.mark.asyncio
async def test_query_users(client: AsyncClient):
    response = await client.get("/api/v1/users/query")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data


@pytest.mark.asyncio
async def test_follow_user_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/users/00000000-0000-0000-0000-000000000000/follow"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_unfollow_user_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/users/00000000-0000-0000-0000-000000000000/follow"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_followers(client: AsyncClient):
    response = await client.get(
        "/api/v1/users/00000000-0000-0000-0000-000000000000/followers"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data


@pytest.mark.asyncio
async def test_get_following(client: AsyncClient):
    response = await client.get(
        "/api/v1/users/00000000-0000-0000-0000-000000000000/following"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
