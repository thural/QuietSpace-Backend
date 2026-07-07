from fastapi import APIRouter, Depends, HTTPException, status, Request
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.models.chat_participant import ChatParticipant
from app.services.chat_service import ChatService
from app.schemas.chat import ChatCreate, ChatResponse
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


@router.get("", response_model=list[ChatResponse])
async def get_chats(current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ChatService(db)
    chats = await service.get_user_chats(current_user.id)
    return chats


@router.get("/{chat_id}", response_model=ChatResponse)
async def get_chat(chat_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ChatService(db)
    chat = await service.get_chat(chat_id)
    if not chat:
        raise HTTPException(status_code=404, detail="Chat not found")
    return chat


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
