from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.chat_service import ChatService
from app.schemas.chat import ChatCreate, ChatResponse

router = APIRouter()


@router.post("/", response_model=ChatResponse, status_code=status.HTTP_201_CREATED)
async def create_chat(chat_in: ChatCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = ChatService(db)
    chat = await service.create_chat(chat_in, current_user.id)
    return chat


@router.get("/", response_model=list[ChatResponse])
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
