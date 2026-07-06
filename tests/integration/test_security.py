from datetime import timedelta, datetime
from uuid import uuid4

import pytest
import pytest_asyncio
from jose import jwt as pyjwt
from httpx import AsyncClient, ASGITransport
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine, async_sessionmaker
from sqlmodel import SQLModel

from app.core.security import create_access_token, hash_password
from app.config.settings import settings
from app.models.user import User

TEST_DB = "postgresql+asyncpg://test:test@localhost:5433/test_db"


@pytest_asyncio.fixture
async def sec_db():
    engine = create_async_engine(TEST_DB, echo=False)
    factory = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)
    async with engine.begin() as conn:
        await conn.run_sync(SQLModel.metadata.drop_all)
        await conn.run_sync(SQLModel.metadata.create_all)
    async with factory() as session:
        yield session, factory
    await engine.dispose()


@pytest_asyncio.fixture
async def sec_client(sec_db):
    session, factory = sec_db
    from app.main import app
    from app.api.deps import get_db

    async def override_get_db():
        yield session

    app.dependency_overrides[get_db] = override_get_db
    original = getattr(app.state, "async_session", None)
    app.state.async_session = factory
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as c:
        yield c
    app.dependency_overrides.clear()
    if original:
        app.state.async_session = original
    elif hasattr(app.state, "async_session"):
        del app.state.async_session


@pytest.mark.asyncio
async def test_no_auth_header(client: AsyncClient):
    resp = await client.get("/api/v1/users/me")
    assert resp.status_code == 403


@pytest.mark.asyncio
async def test_invalid_token(client: AsyncClient):
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": "Bearer this-is-not-a-valid-jwt"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_malformed_token(client: AsyncClient):
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": "Bearer not-even-base64!!!"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_expired_token(client: AsyncClient):
    token = create_access_token({"sub": "anyone@test.com"}, expires_delta=timedelta(minutes=-1))
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_wrong_signature(client: AsyncClient):
    token = pyjwt.encode(
        {"sub": "anyone@test.com", "exp": datetime.utcnow() + timedelta(hours=1), "jti": str(uuid4())},
        "wrong-secret-key",
        algorithm="HS256",
    )
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_missing_sub_claim(client: AsyncClient):
    token = create_access_token({"not_sub": "nobody"})
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_nonexistent_user_in_token(client: AsyncClient):
    token = create_access_token({"sub": "does-not-exist@test.com"})
    resp = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 401


@pytest.mark.asyncio
async def test_token_revocation_on_logout(sec_client: AsyncClient, sec_db):
    session, factory = sec_db
    from app.models.user import User

    email = f"revoke-test-{uuid4().hex[:8]}@test.com"
    reg = await sec_client.post(
        "/api/v1/auth/register",
        json={
            "email": email,
            "username": f"revoketest-{uuid4().hex[:8]}",
            "password": "StrongPass1!",
        },
    )
    assert reg.status_code == 201

    result = await session.execute(select(User).where(User.email == email))
    user = result.scalar_one()
    act = await sec_client.post("/api/v1/auth/activate-account", json={"code": user.activation_code})
    assert act.status_code == 200

    login = await sec_client.post(
        "/api/v1/auth/login",
        json={"username": user.username, "password": "StrongPass1!"},
    )
    assert login.status_code == 200
    token = login.json()["access_token"]

    me = await sec_client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {token}"})
    assert me.status_code == 200

    logout = await sec_client.post("/api/v1/auth/logout", headers={"Authorization": f"Bearer {token}"})
    assert logout.status_code == 200

    me2 = await sec_client.get("/api/v1/users/me", headers={"Authorization": f"Bearer {token}"})
    assert me2.status_code == 401


@pytest.mark.asyncio
async def test_token_without_jti(sec_client: AsyncClient, sec_db):
    session, factory = sec_db
    from app.models.user import User

    email = f"no-jti-{uuid4().hex[:8]}@test.com"
    username = f"nojti-{uuid4().hex[:8]}"
    user = User(
        username=username,
        email=email,
        password_hash=hash_password("StrongPass1!"),
        enabled=True,
    )
    session.add(user)
    await session.commit()
    await session.refresh(user)

    token = pyjwt.encode(
        {"sub": email, "exp": datetime.utcnow() + timedelta(hours=1)},
        settings.SECRET_KEY,
        algorithm="HS256",
    )

    resp = await sec_client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert resp.status_code == 200


@pytest.mark.asyncio
async def test_disabled_account_login(client: AsyncClient, db_session: AsyncSession):
    email = f"disabled-{uuid4().hex[:8]}@test.com"
    reg = await client.post(
        "/api/v1/auth/register",
        json={
            "email": email,
            "username": f"disabled-{uuid4().hex[:8]}",
            "password": "StrongPass1!",
        },
    )
    assert reg.status_code == 201

    result = await db_session.execute(select(User).where(User.email == email))
    u = result.scalar_one()
    login = await client.post(
        "/api/v1/auth/login",
        json={"username": u.username, "password": "StrongPass1!"},
    )
    assert login.status_code == 401
