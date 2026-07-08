import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_create_comment_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/comments",
        json={"text": "Nice post!", "post_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_post_comments(client: AsyncClient):
    response = await client.get(
        "/api/v1/comments/post/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


@pytest.mark.asyncio
async def test_get_comment_not_found(client: AsyncClient):
    response = await client.get("/api/v1/comments/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_get_comments_by_user(client: AsyncClient):
    response = await client.get("/api/v1/comments/user/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)
    assert "has_more" in data


@pytest.mark.asyncio
async def test_get_comments_by_user_nonexistent(client: AsyncClient):
    response = await client.get("/api/v1/comments/user/00000000-0000-0000-0000-000000000001")
    assert response.status_code == 200
    data = response.json()
    assert data["items"] == []


@pytest.mark.asyncio
async def test_delete_comment_not_found(client: AsyncClient):
    response = await client.delete("/api/v1/comments/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 403
