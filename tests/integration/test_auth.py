import pytest
from httpx import AsyncClient
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.user import User


@pytest.mark.asyncio
async def test_register(client: AsyncClient):
    response = await client.post(
        "/api/v1/auth/register",
        json={"username": "testuser", "email": "test@example.com", "password": "password123"},
    )
    assert response.status_code == 201
    data = response.json()
    assert data["username"] == "testuser"
    assert data["email"] == "test@example.com"


@pytest.mark.asyncio
async def test_register_duplicate_email(client: AsyncClient):
    await client.post(
        "/api/v1/auth/register",
        json={"username": "user1", "email": "dup@example.com", "password": "password123"},
    )
    response = await client.post(
        "/api/v1/auth/register",
        json={"username": "user2", "email": "dup@example.com", "password": "password123"},
    )
    assert response.status_code == 400


@pytest.mark.asyncio
async def test_login(client: AsyncClient, db_session: AsyncSession):
    reg_resp = await client.post(
        "/api/v1/auth/register",
        json={"username": "loginuser", "email": "login@example.com", "password": "password123"},
    )
    assert reg_resp.status_code == 201, reg_resp.text

    result = await db_session.execute(select(User).where(User.username == "loginuser"))
    user = result.scalar_one()
    activate_resp = await client.post(
        "/api/v1/auth/activate-account",
        json={"code": user.activation_code},
    )
    assert activate_resp.status_code == 200, activate_resp.text

    response = await client.post(
        "/api/v1/auth/login",
        json={"username": "loginuser", "password": "password123"},
    )
    assert response.status_code == 200, response.text
    data = response.json()
    assert "access_token" in data


@pytest.mark.asyncio
async def test_login_invalid_credentials(client: AsyncClient):
    response = await client.post(
        "/api/v1/auth/login",
        json={"username": "nonexistent", "password": "wrong"},
    )
    assert response.status_code == 401


@pytest.mark.asyncio
async def test_activate_account(client: AsyncClient, db_session: AsyncSession):
    reg_resp = await client.post(
        "/api/v1/auth/register",
        json={"username": "activateuser", "email": "activate@example.com", "password": "password123"},
    )
    assert reg_resp.status_code == 201

    result = await db_session.execute(select(User).where(User.username == "activateuser"))
    user = result.scalar_one()
    activate_resp = await client.post(
        "/api/v1/auth/activate-account",
        json={"code": "invalid-code"},
    )
    assert activate_resp.status_code == 400
