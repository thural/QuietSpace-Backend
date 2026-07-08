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
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)
    assert "has_more" in data


@pytest.mark.asyncio
async def test_get_post_not_found(client: AsyncClient):
    response = await client.get("/api/v1/posts/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 404


@pytest.mark.asyncio
async def test_get_posts_by_user(client: AsyncClient):
    response = await client.get("/api/v1/posts/user/00000000-0000-0000-0000-000000000000")
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)
    assert "has_more" in data


@pytest.mark.asyncio
async def test_get_posts_by_user_nonexistent(client: AsyncClient):
    response = await client.get("/api/v1/posts/user/00000000-0000-0000-0000-000000000001")
    assert response.status_code == 200
    data = response.json()
    assert data["items"] == []


@pytest.mark.asyncio
async def test_search_posts(client: AsyncClient):
    response = await client.get("/api/v1/posts/search", params={"q": "test"})
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)
    assert "has_more" in data


@pytest.mark.asyncio
async def test_search_posts_empty_query(client: AsyncClient):
    response = await client.get("/api/v1/posts/search")
    assert response.status_code == 422


@pytest.mark.asyncio
async def test_search_posts_with_cursor(client: AsyncClient):
    response = await client.get("/api/v1/posts/search", params={"q": "hello", "limit": 1})
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data["items"], list)
    if data["next_cursor"]:
        response2 = await client.get(
            "/api/v1/posts/search",
            params={"q": "hello", "cursor": data["next_cursor"], "limit": 1},
        )
        assert response2.status_code == 200


@pytest.mark.asyncio
async def test_get_post_comments_nested(client: AsyncClient):
    response = await client.get(
        "/api/v1/posts/00000000-0000-0000-0000-000000000000/comments"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


@pytest.mark.asyncio
async def test_create_post_comment_nested_requires_auth(client: AsyncClient):
    response = await client.post(
        "/api/v1/posts/00000000-0000-0000-0000-000000000000/comments",
        json={"text": "Nice post!", "post_id": "00000000-0000-0000-0000-000000000000"},
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_get_post_reactions_nested(client: AsyncClient):
    response = await client.get(
        "/api/v1/posts/00000000-0000-0000-0000-000000000000/reactions"
    )
    assert response.status_code == 200
    assert isinstance(response.json(), list)


@pytest.mark.asyncio
async def test_get_post_reaction_count_nested(client: AsyncClient):
    response = await client.get(
        "/api/v1/posts/00000000-0000-0000-0000-000000000000/reactions/count"
    )
    assert response.status_code == 200
    data = response.json()
    assert "count" in data
