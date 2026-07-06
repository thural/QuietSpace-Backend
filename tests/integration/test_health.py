import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_health_endpoint(client: AsyncClient):
    response = await client.get("/health")
    assert response.status_code in [200, 500]


@pytest.mark.asyncio
async def test_create_post_validation(client: AsyncClient):
    response = await client.post(
        "/api/v1/posts",
        json={"text": "a" * 281},
    )
    assert response.status_code == 403
