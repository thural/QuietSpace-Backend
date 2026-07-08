from fastapi import APIRouter, Depends, HTTPException, status, Query, UploadFile, File, Request
from fastapi.responses import FileResponse
from sqlalchemy.ext.asyncio import AsyncSession
from uuid import UUID
import os
from app.api.deps import get_db, get_current_user
from app.models.user import User
from app.core.rate_limiter import limiter, CONTENT_LIMIT
from app.services.photo_service import PhotoService
from app.schemas.photo import PhotoCreate, PhotoResponse

router = APIRouter()
UPLOAD_DIR = "uploads/photos"


@router.post("", response_model=PhotoResponse, status_code=status.HTTP_201_CREATED)
@limiter.limit(CONTENT_LIMIT)
async def create_photo(request: Request, photo_in: PhotoCreate, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    photo = await service.create_photo(photo_in)
    return photo


@router.post("/profile", response_model=PhotoResponse)
@limiter.limit(CONTENT_LIMIT)
async def upload_profile_photo(
    request: Request,
    file: UploadFile = File(...),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    os.makedirs(UPLOAD_DIR, exist_ok=True)
    file_path = os.path.join(UPLOAD_DIR, f"profile_{current_user.id}_{file.filename}")
    content = await file.read()
    with open(file_path, "wb") as f:
        f.write(content)
    from app.repositories.profile_settings import ProfileSettingsRepository
    repo = ProfileSettingsRepository(db)
    settings = await repo.get_by_user_id(current_user.id)
    if not settings:
        from app.models.profile_settings import ProfileSettings
        settings = ProfileSettings(user_id=current_user.id)
        db.add(settings)
    settings.avatar_url = file_path
    await repo.update(settings)
    return settings


@router.delete("/profile/{user_id}", status_code=status.HTTP_204_NO_CONTENT)
@limiter.limit(CONTENT_LIMIT)
async def delete_profile_photo(
    request: Request,
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db),
):
    service = PhotoService(db)
    success = await service.delete_profile_photo(user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Profile photo not found")


@router.get("/post/{post_id}", response_model=list[PhotoResponse])
async def get_photos(post_id: UUID, db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    photos = await service.get_photos(post_id)
    return photos


@router.get("/{filename}")
async def get_photo_file(filename: str):
    file_path = os.path.join(UPLOAD_DIR, filename)
    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="Photo not found")
    return FileResponse(file_path)


@router.delete("/{photo_id}", status_code=status.HTTP_204_NO_CONTENT)
@limiter.limit(CONTENT_LIMIT)
async def delete_photo(request: Request, photo_id: UUID, current_user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    service = PhotoService(db)
    success = await service.delete_photo(photo_id)
    if not success:
        raise HTTPException(status_code=404, detail="Photo not found")
