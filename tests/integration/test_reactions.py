import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_toggle_reaction_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/reactions",
        json={"type": "LIKE", "post_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_post_reactions(client: AsyncClient):
    response = await client.get(
        "/api/v1/reactions/post/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    assert isinstance(response.json(), list)


@pytest.mark.asyncio
async def test_get_user_reactions_requires_auth(client: AsyncClient):
    response = await client.get("/api/v1/reactions/user")
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_reaction_count(client: AsyncClient):
    response = await client.get(
        "/api/v1/reactions/count/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    data = response.json()
    assert "count" in data
