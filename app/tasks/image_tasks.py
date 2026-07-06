from app.celery_app import celery_app
from PIL import Image
import io


@celery_app.task
def process_image_upload(image_data: bytes, user_id: str) -> dict:
    try:
        img = Image.open(io.BytesIO(image_data))
        img.thumbnail((800, 800))
        output = io.BytesIO()
        img.save(output, format="JPEG", quality=85, optimize=True)
        return {"status": "success", "user_id": user_id}
    except Exception as e:
        return {"status": "error", "message": str(e)}


@celery_app.task
def send_bulk_notifications(user_ids: list[str], message: str) -> dict:
    return {"processed": len(user_ids)}
