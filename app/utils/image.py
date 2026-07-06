from PIL import Image
import io


def compress_image(image_data: bytes, max_size: tuple = (800, 800), quality: int = 85) -> bytes:
    img = Image.open(io.BytesIO(image_data))
    img.thumbnail(max_size)
    output = io.BytesIO()
    img.save(output, format="JPEG", quality=quality, optimize=True)
    return output.getvalue()
