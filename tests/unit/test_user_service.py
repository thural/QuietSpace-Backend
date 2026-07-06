import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from uuid import UUID, uuid4


@pytest.fixture
def session():
    s = MagicMock()
    s.execute = AsyncMock()
    s.add = MagicMock()
    s.delete = AsyncMock()
    s.commit = AsyncMock()
    return s


@pytest.fixture
def user_repo():
    return AsyncMock()


@pytest.fixture
def service(session, user_repo):
    with patch("app.services.user_service.UserRepository", return_value=user_repo):
        from app.services.user_service import UserService
        yield UserService(session)


@pytest.mark.asyncio
async def test_follow_user_success(service, session):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    session.execute.return_value = mock_result

    result = await service.follow_user(uuid4(), uuid4())
    assert result is True
    session.add.assert_called_once()
    session.commit.assert_awaited_once()


@pytest.mark.asyncio
async def test_follow_self(service):
    user_id = uuid4()
    result = await service.follow_user(user_id, user_id)
    assert result is False


@pytest.mark.asyncio
async def test_follow_already_following(service, session):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = MagicMock()
    session.execute.return_value = mock_result

    result = await service.follow_user(uuid4(), uuid4())
    assert result is False
    session.add.assert_not_called()


@pytest.mark.asyncio
async def test_unfollow_user_success(service, session):
    existing_follow = MagicMock()
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = existing_follow
    session.execute.return_value = mock_result

    result = await service.unfollow_user(uuid4(), uuid4())
    assert result is True
    session.delete.assert_called_once_with(existing_follow)
    session.commit.assert_awaited_once()


@pytest.mark.asyncio
async def test_unfollow_not_following(service, session):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    session.execute.return_value = mock_result

    result = await service.unfollow_user(uuid4(), uuid4())
    assert result is False


@pytest.mark.asyncio
async def test_get_user(service, user_repo):
    user_id = uuid4()
    await service.get_user(str(user_id))
    user_repo.get.assert_awaited_once_with(user_id)


@pytest.mark.asyncio
async def test_search_users(service, user_repo):
    await service.search_users("query")
    user_repo.search.assert_awaited_once_with("query", 20)


@pytest.mark.asyncio
async def test_get_followers(service, user_repo):
    user_id = uuid4()
    await service.get_followers(user_id)
    user_repo.get_followers.assert_awaited_once_with(user_id)


@pytest.mark.asyncio
async def test_get_following(service, user_repo):
    user_id = uuid4()
    await service.get_following(user_id)
    user_repo.get_following.assert_awaited_once_with(user_id)
