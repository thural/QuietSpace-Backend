from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.notification_service import NotificationService
from app.schemas.notification import NotificationResponse

router = APIRouter()


@router.get("", response_model=list[NotificationResponse])
async def get_notifications(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
    limit: int = Query(50, ge=1, le=100),
    offset: int = Query(0, ge=0),
    type: str | None = Query(None),
):
    service = NotificationService(db)
    notifications = await service.get_notifications(current_user.id, limit=limit, offset=offset, type_filter=type)
    return notifications


@router.get("/unread/count")
async def get_unread_count(current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = NotificationService(db)
    count = await service.get_unread_count(current_user.id)
    return {"count": count}


@router.put("/{notification_id}/read", response_model=NotificationResponse)
async def mark_as_read(notification_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = NotificationService(db)
    notification = await service.mark_as_read(notification_id, actor_id=current_user.id)
    if not notification:
        raise HTTPException(status_code=404, detail="Notification not found")
    return notification
