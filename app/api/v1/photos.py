from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.services.photo_service import PhotoService
from app.schemas.photo import PhotoCreate, PhotoResponse

router = APIRouter()


@router.post("/", response_model=PhotoResponse, status_code=status.HTTP_201_CREATED)
async def create_photo(photo_in: PhotoCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    photo = await service.create_photo(photo_in)
    return photo


@router.get("/post/{post_id}", response_model=list[PhotoResponse])
async def get_photos(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    photos = await service.get_photos(post_id)
    return photos


@router.delete("/{photo_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_photo(photo_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    success = await service.delete_photo(photo_id)
    if not success:
        raise HTTPException(status_code=404, detail="Photo not found")
