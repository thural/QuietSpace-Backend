import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from uuid import UUID, uuid4
from datetime import datetime, timedelta


@pytest.fixture
def mock_user():
    user = MagicMock()
    user.id = uuid4()
    user.email = "test@example.com"
    user.username = "testuser"
    user.enabled = False
    user.password_hash = "hashed_pass"
    user.activation_code = str(uuid4())
    user.activation_code_expires_at = datetime.utcnow() + timedelta(hours=24)
    return user


@pytest.fixture
def mock_session():
    return AsyncMock()


@pytest.fixture
def auth_service(mock_session):
    from app.services.auth_service import AuthService
    with (
        patch("app.services.auth_service.UserRepository") as mock_user_repo_cls,
        patch("app.services.auth_service.TokenRepository") as mock_token_repo_cls,
    ):
        service = AuthService(mock_session)
        service.user_repo = AsyncMock()
        service.token_repo = AsyncMock()
        yield service


@pytest.mark.asyncio
async def test_register_success(auth_service, mock_user):
    auth_service.user_repo.get_by_email.return_value = None
    auth_service.user_repo.get_by_username.return_value = None
    auth_service.user_repo.create.return_value = mock_user

    with patch("app.services.auth_service.hash_password", return_value="hashed_pass"):
        result = await auth_service.register("testuser", "test@example.com", "password123")
        assert result == mock_user
        assert not result.enabled


@pytest.mark.asyncio
async def test_register_duplicate_email(auth_service, mock_user):
    auth_service.user_repo.get_by_email.return_value = mock_user
    with pytest.raises(ValueError, match="Email already registered"):
        await auth_service.register("testuser", "test@example.com", "password123")


@pytest.mark.asyncio
async def test_register_duplicate_username(auth_service, mock_user):
    auth_service.user_repo.get_by_email.return_value = None
    auth_service.user_repo.get_by_username.return_value = mock_user
    with pytest.raises(ValueError, match="Username already taken"):
        await auth_service.register("testuser", "test@example.com", "password123")


@pytest.mark.asyncio
async def test_activate_account_success(auth_service, mock_user):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = mock_user
    auth_service.session.execute = AsyncMock(return_value=mock_result)
    auth_service.user_repo.update = AsyncMock(return_value=mock_user)

    result = await auth_service.activate_account(mock_user.activation_code)
    assert result is mock_user
    assert result.enabled is True


@pytest.mark.asyncio
async def test_activate_account_invalid_code(auth_service):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    auth_service.session.execute = AsyncMock(return_value=mock_result)

    result = await auth_service.activate_account("invalid_code")
    assert result is None


@pytest.mark.asyncio
async def test_activate_account_expired_code(auth_service, mock_user):
    mock_user.activation_code_expires_at = datetime.utcnow() - timedelta(hours=1)
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = mock_user
    auth_service.session.execute = AsyncMock(return_value=mock_result)

    result = await auth_service.activate_account(mock_user.activation_code)
    assert result is None


@pytest.mark.asyncio
async def test_login_success(auth_service, mock_user):
    mock_user.enabled = True
    auth_service.user_repo.get_by_username.return_value = mock_user
    auth_service.token_repo.create_token = AsyncMock()

    with (
        patch("app.services.auth_service.verify_password", return_value=True),
        patch("app.services.auth_service.create_access_token", return_value="jwt_token"),
        patch("app.services.auth_service.decode_token", return_value={"jti": "test-jti", "exp": datetime.utcnow().timestamp()}),
    ):
        result = await auth_service.login("testuser", "password123")
        assert result["access_token"] == "jwt_token"
        assert result["token_type"] == "bearer"
        assert result["user"] == mock_user


@pytest.mark.asyncio
async def test_login_invalid_credentials(auth_service):
    auth_service.user_repo.get_by_username.return_value = None
    with pytest.raises(ValueError, match="Invalid credentials"):
        await auth_service.login("testuser", "wrong")


@pytest.mark.asyncio
async def test_login_disabled_account(auth_service, mock_user):
    mock_user.enabled = False
    auth_service.user_repo.get_by_username.return_value = mock_user
    with patch("app.services.auth_service.verify_password", return_value=True):
        with pytest.raises(ValueError, match="Account is disabled"):
            await auth_service.login("testuser", "password123")


@pytest.mark.asyncio
async def test_refresh_token_success(auth_service, mock_user):
    mock_token = MagicMock()
    mock_token.revoked = False
    auth_service.token_repo.get_by_jti.return_value = mock_token
    auth_service.user_repo.get_by_email.return_value = mock_user
    auth_service.token_repo.create_token = AsyncMock()

    with (
        patch("app.services.auth_service.decode_token") as mock_decode,
        patch("app.services.auth_service.create_access_token", return_value="new_jwt"),
    ):
        mock_decode.side_effect = [
            {"jti": "old-jti", "sub": "test@example.com"},
            {"jti": "new-jti", "exp": datetime.utcnow().timestamp()},
        ]
        result = await auth_service.refresh_token("old_token")
        assert result["access_token"] == "new_jwt"
        mock_token.revoked is True


@pytest.mark.asyncio
async def test_refresh_token_revoked(auth_service):
    mock_token = MagicMock()
    mock_token.revoked = True
    auth_service.token_repo.get_by_jti.return_value = mock_token

    with patch("app.services.auth_service.decode_token", return_value={"jti": "revoked-jti"}):
        with pytest.raises(ValueError, match="Token has been revoked"):
            await auth_service.refresh_token("old_token")


@pytest.mark.asyncio
async def test_logout_success(auth_service):
    mock_token_record = MagicMock()
    auth_service.token_repo.get_by_jti.return_value = mock_token_record
    auth_service.token_repo.update = AsyncMock()

    with patch("app.services.auth_service.decode_token", return_value={"jti": "some-jti"}):
        await auth_service.logout("valid_token")
        assert mock_token_record.revoked is True
        auth_service.token_repo.update.assert_awaited_once_with(mock_token_record)


@pytest.mark.asyncio
async def test_logout_invalid_token(auth_service):
    from jose import JWTError
    with patch("app.services.auth_service.decode_token", side_effect=JWTError("bad")):
        await auth_service.logout("bad_token")
