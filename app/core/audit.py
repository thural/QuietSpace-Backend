from datetime import datetime, timezone
from uuid import UUID


class AuditService:
    def __init__(self, current_user_id: UUID | None = None):
        self.current_user_id = current_user_id

    def populate_create(self, obj) -> None:
        obj.created_by = self.current_user_id
        obj.updated_by = self.current_user_id

    def populate_update(self, obj) -> None:
        obj.updated_at = datetime.now(timezone.utc)
        obj.updated_by = self.current_user_id
