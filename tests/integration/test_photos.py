import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_post_photos(client: AsyncClient):
    response = await client.get(
        "/api/v1/photos/post/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    assert isinstance(response.json(), list)
