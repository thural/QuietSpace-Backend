from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.notification_service import NotificationService
from app.schemas.notification import NotificationResponse, BatchReadRequest
from app.schemas.pagination import CursorResponse

router = APIRouter()


@router.get("", response_model=CursorResponse[NotificationResponse], summary="Get user's notifications (cursor paginated)")
async def get_notifications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    cursor: str | None = Query(None),
    limit: int = Query(50, ge=1, le=100),
    type: str | None = Query(None),
):
    service = NotificationService(db)
    notifications, next_cursor, has_more = await service.get_notifications(current_user.id, cursor=cursor, limit=limit, type_filter=type)
    return CursorResponse(items=notifications, next_cursor=next_cursor, has_more=has_more)


@router.get("/unread/count", summary="Get count of unread notifications")
async def get_unread_count(current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = NotificationService(db)
    count = await service.get_unread_count(current_user.id)
    return {"count": count}


@router.put("/read", status_code=status.HTTP_200_OK, summary="Mark multiple notifications as read")
async def mark_multiple_as_read(
    body: BatchReadRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = NotificationService(db)
    marked = await service.mark_multiple_as_read(
        current_user.id, ids=body.ids, all_=body.all or False
    )
    return {"marked": marked}


@router.put("/{notification_id}/read", response_model=NotificationResponse, summary="Mark a single notification as read")
async def mark_as_read(notification_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = NotificationService(db)
    notification = await service.mark_as_read(notification_id, actor_id=current_user.id)
    if not notification:
        raise HTTPException(status_code=404, detail="Notification not found")
    return notification
