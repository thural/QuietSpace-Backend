import base64
from datetime import datetime
from uuid import UUID


def encode_cursor(timestamp: datetime, id: UUID) -> str:
    raw = f"{timestamp.isoformat()}|{id}"
    return base64.urlsafe_b64encode(raw.encode()).decode()


def decode_cursor(cursor: str) -> tuple[datetime, UUID]:
    raw = base64.urlsafe_b64decode(cursor.encode()).decode()
    ts_str, id_str = raw.rsplit("|", 1)
    ts = datetime.fromisoformat(ts_str)
    return ts, UUID(id_str)
