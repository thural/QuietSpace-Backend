import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from uuid import uuid4


@pytest.fixture
def mock_session():
    session = MagicMock()
    session.execute = AsyncMock()
    session.add = MagicMock()
    session.commit = AsyncMock()
    session.refresh = AsyncMock()
    session.delete = AsyncMock()
    return session


@pytest.fixture
def repo(mock_session):
    from app.repositories.base import BaseRepository
    model_cls = MagicMock()
    model_cls.id = MagicMock()
    return BaseRepository(model_cls, mock_session)


@pytest.mark.asyncio
async def test_get_found(repo, mock_session):
    mock_result = MagicMock()
    expected = MagicMock()
    mock_result.scalar_one_or_none.return_value = expected
    mock_session.execute.return_value = mock_result

    with patch("app.repositories.base.select") as mock_select:
        mock_select.return_value = MagicMock()
        result = await repo.get(uuid4())
        assert result == expected
        mock_session.execute.assert_awaited_once()


@pytest.mark.asyncio
async def test_get_not_found(repo, mock_session):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    mock_session.execute.return_value = mock_result

    with patch("app.repositories.base.select") as mock_select:
        mock_select.return_value = MagicMock()
        result = await repo.get(uuid4())
        assert result is None


@pytest.mark.asyncio
async def test_get_all(repo, mock_session):
    mock_result = MagicMock()
    mock_result.scalars.return_value.all.return_value = ["item1", "item2"]
    mock_session.execute.return_value = mock_result

    with patch("app.repositories.base.select") as mock_select:
        mock_select.return_value = MagicMock()
        result = await repo.get_all()
        assert result == ["item1", "item2"]


@pytest.mark.asyncio
async def test_create(repo, mock_session):
    obj = MagicMock()
    result = await repo.create(obj)
    assert result == obj
    mock_session.add.assert_called_once_with(obj)
    mock_session.commit.assert_awaited_once()
    mock_session.refresh.assert_awaited_once_with(obj)


@pytest.mark.asyncio
async def test_update(repo, mock_session):
    obj = MagicMock()
    result = await repo.update(obj)
    assert result == obj
    mock_session.commit.assert_awaited_once()
    mock_session.refresh.assert_awaited_once_with(obj)


@pytest.mark.asyncio
async def test_delete_found(repo, mock_session):
    obj = MagicMock()
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = obj
    mock_session.execute.return_value = mock_result

    with patch("app.repositories.base.select") as mock_select:
        mock_select.return_value = MagicMock()
        result = await repo.delete(uuid4())
        assert result is True
        mock_session.delete.assert_called_once_with(obj)
        mock_session.commit.assert_awaited_once()


@pytest.mark.asyncio
async def test_delete_not_found(repo, mock_session):
    mock_result = MagicMock()
    mock_result.scalar_one_or_none.return_value = None
    mock_session.execute.return_value = mock_result

    with patch("app.repositories.base.select") as mock_select:
        mock_select.return_value = MagicMock()
        result = await repo.delete(uuid4())
        assert result is False
        mock_session.delete.assert_not_called()
