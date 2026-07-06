from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.models.chat_participant import ChatParticipant
from app.services.chat_service import ChatService
from app.schemas.chat import ChatCreate, ChatResponse

router = APIRouter()


@router.post("", response_model=ChatResponse, status_code=status.HTTP_201_CREATED)
async def create_chat(chat_in: ChatCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
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
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == current_user.id,
        )
    )
    is_member = result.scalar_one_or_none()
    if not is_member:
        raise HTTPException(status_code=403, detail="Not a chat member")
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == user_id,
        )
    )
    existing = result.scalar_one_or_none()
    if existing:
        raise HTTPException(status_code=400, detail="User already a participant")
    participant = ChatParticipant(chat_id=chat_id, user_id=user_id)
    db.add(participant)
    await db.commit()
    return {"message": "Participant added"}


@router.delete("/{chat_id}/participants/{user_id}")
async def remove_participant(
    chat_id: UUID,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == current_user.id,
        )
    )
    is_member = result.scalar_one_or_none()
    if not is_member:
        raise HTTPException(status_code=403, detail="Not a chat member")
    result = await db.execute(
        select(ChatParticipant).where(
            ChatParticipant.chat_id == chat_id,
            ChatParticipant.user_id == user_id,
        )
    )
    participant = result.scalar_one_or_none()
    if not participant:
        raise HTTPException(status_code=404, detail="Participant not found")
    await db.delete(participant)
    await db.commit()
    return {"message": "Participant removed"}
