import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_create_reaction_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/reactions",
        json={"type": "LIKE", "post_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_delete_reaction_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/reactions/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_delete_reaction_not_found(client: AsyncClient):
    from app.main import app
    from app.api.deps import get_current_user
    from app.models.user import User

    user = User(username="reactiondel", email="reactiondel@test.com", password_hash="x")
    # no db_session needed; we just need auth context
    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.delete(
        "/api/v1/reactions/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 404
    del app.dependency_overrides[get_current_user]


@pytest.mark.asyncio
async def test_get_post_reactions(client: AsyncClient):
    response = await client.get(
        "/api/v1/reactions/post/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


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


@pytest.mark.asyncio
async def test_get_content_reactions_post(client: AsyncClient):
    response = await client.get(
        "/api/v1/reactions/content",
        params={"content_type": "post", "content_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


@pytest.mark.asyncio
async def test_get_content_reactions_invalid_type(client: AsyncClient):
    response = await client.get(
        "/api/v1/reactions/content",
        params={"content_type": "invalid", "content_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 422
