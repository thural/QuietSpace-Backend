import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from uuid import uuid4
from datetime import datetime


@pytest.fixture
def session():
    return MagicMock()


@pytest.fixture
def notification_repo():
    return AsyncMock()


@pytest.fixture
def service(session, notification_repo):
    with patch("app.services.notification_service.NotificationRepository", return_value=notification_repo):
        from app.services.notification_service import NotificationService
        yield NotificationService(session)


@pytest.mark.asyncio
async def test_create_notification(service, notification_repo):
    from app.schemas.notification import NotificationCreate

    mock_notification = MagicMock()
    mock_notification.user_id = uuid4()
    mock_notification.model_dump.return_value = {"id": "n1"}
    notification_repo.create.return_value = mock_notification

    with patch("app.api.websocket.manager.manager") as mock_manager:
        mock_manager.send_to_user = AsyncMock()
        notification_in = NotificationCreate(
            user_id=uuid4(), type="LIKE", title="New Like", content="Someone liked your post"
        )
        result = await service.create_notification(notification_in)
        assert result == mock_notification
        notification_repo.create.assert_awaited_once()
        mock_manager.send_to_user.assert_awaited_once_with(
            mock_notification.user_id, "new_notification", {"id": "n1"}
        )


@pytest.mark.asyncio
async def test_get_notifications(service, notification_repo):
    user_id = uuid4()
    notification_repo.get_by_user.return_value = ["n1", "n2"]
    result = await service.get_notifications(user_id, limit=10, offset=0, type_filter="LIKE")
    assert result == ["n1", "n2"]
    notification_repo.get_by_user.assert_awaited_once_with(user_id, 10, 0, "LIKE")


@pytest.mark.asyncio
async def test_get_unread_count(service, notification_repo):
    notification_repo.get_unread_count.return_value = 3
    result = await service.get_unread_count(uuid4())
    assert result == 3


@pytest.mark.asyncio
async def test_mark_as_read(service, notification_repo):
    notification = MagicMock()
    notification.read = False
    notification.read_at = None
    notification_repo.get.return_value = notification
    notification_repo.update.return_value = notification

    result = await service.mark_as_read(uuid4())
    assert result is notification
    assert notification.read is True
    assert notification.read_at is not None
    notification_repo.update.assert_awaited_once_with(notification)
