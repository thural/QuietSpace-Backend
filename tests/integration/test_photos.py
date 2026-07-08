import pytest
from httpx import AsyncClient
from app.main import app


@pytest.mark.asyncio
async def test_get_post_photos(client: AsyncClient):
    response = await client.get(
        "/api/v1/photos/post/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 200
    data = response.json()
    assert "items" in data
    assert isinstance(data["items"], list)


@pytest.mark.asyncio
async def test_delete_profile_photo_requires_auth(client: AsyncClient):
    response = await client.delete(
        "/api/v1/photos/profile/00000000-0000-0000-0000-000000000000"
    )
    assert response.status_code == 403


@pytest.mark.asyncio
async def test_delete_profile_photo_not_found(client: AsyncClient, db_session):
    from app.api.deps import get_current_user
    from app.models.user import User

    user = User(
        username="photouser",
        email="photouser@test.com",
        password_hash="x",
    )
    db_session.add(user)
    await db_session.commit()
    await db_session.refresh(user)

    app.dependency_overrides[get_current_user] = lambda: user
    response = await client.delete(f"/api/v1/photos/profile/{user.id}")
    assert response.status_code == 404

    del app.dependency_overrides[get_current_user]
