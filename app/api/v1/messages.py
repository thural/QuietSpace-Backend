from fastapi import APIRouter, Depends, HTTPException, status, Query, Request
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user, get_optional_current_user
from app.models.user import User
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.services.message_service import MessageService
from app.schemas.message import MessageCreate, MessageResponse
from app.schemas.pagination import CursorResponse

router = APIRouter()


@router.get("/{message_id}", response_model=MessageResponse)
async def get_message(
    message_id: UUID,
    current_user: User | None = Depends(get_optional_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = MessageService(db)
    user_id = current_user.id if current_user else None
    message = await service.get_message(message_id, current_user_id=user_id)
    if not message:
        raise HTTPException(status_code=404, detail="Message not found")
    return message


@router.post("", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def send_message(request: Request, message_in: MessageCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if message_in.sender_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    service = MessageService(db)
    try:
        message = await service.add_message(message_in.model_dump())
    except ValueError as e:
        raise HTTPException(status_code=403, detail=str(e))
    return message


@router.get("/chat/{chat_id}", response_model=CursorResponse[MessageResponse])
async def get_messages(chat_id: UUID, current_user: User = Depends(get_current_user), cursor: str | None = Query(None), limit: int = Query(50, ge=1, le=100), db: AsyncSession = Depends(get_db)):
    service = MessageService(db)
    messages, next_cursor, has_more = await service.get_messages(chat_id, cursor=cursor, limit=limit, current_user_id=current_user.id)
    return CursorResponse(items=messages, next_cursor=next_cursor, has_more=has_more)


@router.get("/unread", response_model=CursorResponse[MessageResponse])
async def get_unread_messages(
    current_user: User = Depends(get_current_user),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = MessageService(db)
    messages, next_cursor, has_more = await service.get_unread(current_user.id, cursor=cursor, limit=limit)
    return CursorResponse(items=messages, next_cursor=next_cursor, has_more=has_more)


@router.delete("/{message_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_message(
    message_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    from app.core.unit_of_work import UnitOfWork
    from app.models.websocket_event import EventFactory
    from app.enums.websocket_event_type import WebSocketEventType
    from app.enums.role import Role

    service = MessageService(db)
    try:
        message = await service.delete_message(message_id, current_user.id, is_admin=current_user.role == Role.ADMIN)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail=str(e))
    async with UnitOfWork(db) as uow:
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.DELETE_MESSAGE,
            actor_id=current_user.id,
            chat_id=message.chat_id,
            message_id=message_id,
        )
        uow.add_event(event)
        await uow.commit()


@router.put("/{message_id}/read", response_model=dict)
async def mark_message_read(
    message_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    from app.core.unit_of_work import UnitOfWork
    from app.models.websocket_event import EventFactory
    from app.enums.websocket_event_type import WebSocketEventType

    service = MessageService(db)
    result = await service.mark_as_read(message_id, current_user.id)
    if not result:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Message not found")
    sender_id, chat_id = result
    async with UnitOfWork(db) as uow:
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.SEEN_MESSAGE,
            actor_id=current_user.id,
            chat_id=chat_id,
            message_id=message_id,
            recipient_id=sender_id,
        )
        uow.add_event(event)
        await uow.commit()
    return {"message": "Message marked as read"}
