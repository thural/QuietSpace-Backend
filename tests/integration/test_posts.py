import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_create_post_validation(client: AsyncClient):
    response = await client.post(
        "/api/v1/posts",
        json={"text": "a" * 281},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_posts(client: AsyncClient):
    response = await client.get("/api/v1/posts")
    assert response.status_code == 200
    assert isinstance(response.json(), list)


@pytest.mark.asyncio
async def test_get_post_not_found(client: AsyncClient):
    response = await client.get("/api/v1/posts/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404
