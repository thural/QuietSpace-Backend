from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.message_service import MessageService
from app.schemas.message import MessageCreate, MessageResponse

router = APIRouter()


@router.post("", response_model=MessageResponse, status_code=status.HTTP_201_CREATED)
async def send_message(message_in: MessageCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    if message_in.sender_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    service = MessageService(db)
    message = await service.add_message(message_in.model_dump())
    return message


@router.get("/chat/{chat_id}", response_model=list[MessageResponse])
async def get_messages(chat_id: UUID, current_user: User = Depends(get_current_user), limit: int = Query(50, ge=1, le=100), offset: int = Query(0, ge=0), db: AsyncSession = Depends(get_db)):
    service = MessageService(db)
    messages = await service.get_messages(chat_id, limit=limit, offset=offset)
    return messages


@router.get("/unread", response_model=list[MessageResponse])
async def get_unread_messages(current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = MessageService(db)
    messages = await service.get_unread(current_user.id)
    return messages
