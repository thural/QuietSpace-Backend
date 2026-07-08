from fastapi import APIRouter, Depends, HTTPException, Query, status, Request
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.models.chat_participant import ChatParticipant
from app.services.chat_service import ChatService
from app.services.message_service import MessageService
from app.schemas.chat import ChatCreate, ChatUpdate, ChatResponse
from app.schemas.message import MessageCreate, MessageResponse
from app.schemas.pagination import CursorResponse
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.core.unit_of_work import UnitOfWork
from app.models.websocket_event import EventFactory
from app.enums.websocket_event_type import WebSocketEventType

router = APIRouter()


@router.post("", response_model=ChatResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_chat(request: Request, chat_in: ChatCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ChatService(db)
    chat = await service.create_chat(chat_in, current_user.id)
    participant = ChatParticipant(chat_id=chat.id, user_id=current_user.id)
    db.add(participant)
    await db.commit()
    return chat


@router.get("", response_model=CursorResponse[ChatResponse])
async def get_chats(
    current_user: User = Depends(get_current_user),
    cursor: str | None = Query(None),
    limit: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = ChatService(db)
    chats, next_cursor, has_more = await service.get_user_chats(current_user.id, cursor=cursor, limit=limit)
    return CursorResponse(items=chats, next_cursor=next_cursor, has_more=has_more)


@router.get("/{chat_id}", response_model=ChatResponse)
async def get_chat(chat_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ChatService(db)
    chat = await service.get_chat(chat_id)
    if not chat:
        raise HTTPException(status_code=404, detail="Chat not found")
    return chat


@router.get("/{chat_id}/messages", response_model=CursorResponse[MessageResponse])
async def get_chat_messages(
    chat_id: UUID,
    current_user: User = Depends(get_current_user),
    cursor: str | None = Query(None),
    limit: int = Query(50, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    service = MessageService(db)
    messages, next_cursor, has_more = await service.get_messages(chat_id, cursor=cursor, limit=limit, current_user_id=current_user.id)
    return CursorResponse(items=messages, next_cursor=next_cursor, has_more=has_more)


@router.post("/{chat_id}/messages", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def send_chat_message(request: Request, chat_id: UUID, message_in: MessageCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if message_in.chat_id != chat_id:
        raise HTTPException(status_code=400, detail="chat_id in body does not match path")
    service = MessageService(db)
    try:
        message = await service.add_message({**message_in.model_dump(), "sender_id": current_user.id})
    except ValueError as e:
        raise HTTPException(status_code=403, detail=str(e))
    return message


@router.patch("/{chat_id}", response_model=ChatResponse)
@limiter.limit(CONTENT_LIMIT)
async def update_chat(request: Request, chat_id: UUID, chat_in: ChatUpdate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == current_user.id,
        )
    )
    if not result.scalar_one_or_none():
        raise HTTPException(status_code=403, detail="Not a chat member")
    service = ChatService(db)
    chat = await service.update_chat(chat_id, chat_in)
    if not chat:
        raise HTTPException(status_code=404, detail="Chat not found")
    return chat


@router.delete("/{chat_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_chat(
    chat_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = ChatService(db)
    try:
        deleted = await service.delete_chat(chat_id, current_user.id)
    except ValueError as e:
        raise HTTPException(status_code=403, detail=str(e))
    if not deleted:
        raise HTTPException(status_code=404, detail="Chat not found")


@router.post("/{chat_id}/participants")
async def add_participant(
    chat_id: UUID,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = ChatService(db)
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == current_user.id,
        )
    )
    is_member = result.scalar_one_or_none()
    if not is_member:
        raise HTTPException(status_code=403, detail="Not a chat member")
    async with UnitOfWork(db) as uow:
        added = await service.add_member(chat_id, user_id, current_user.id)
        if not added:
            raise HTTPException(status_code=400, detail="User already a participant")
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.JOIN_CHAT,
            actor_id=current_user.id,
            chat_id=chat_id,
        )
        uow.add_event(event)
        await uow.commit()
    return {"message": "Participant added"}


@router.delete("/{chat_id}/participants/{user_id}")
async def remove_participant(
    chat_id: UUID,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = ChatService(db)
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == current_user.id,
        )
    )
    is_member = result.scalar_one_or_none()
    if not is_member:
        raise HTTPException(status_code=403, detail="Not a chat member")
    async with UnitOfWork(db) as uow:
        removed = await service.remove_member(chat_id, user_id)
        if not removed:
            raise HTTPException(status_code=404, detail="Participant not found")
        event = EventFactory.create_chat_event(
            event_type=WebSocketEventType.LEAVE_CHAT,
            actor_id=current_user.id,
            chat_id=chat_id,
        )
        uow.add_event(event)
        await uow.commit()
    return {"message": "Participant removed"}
