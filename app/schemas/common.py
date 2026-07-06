from pydantic import BaseModel
from typing import Optional, List, Dict, Any


class PaginatedResponse(BaseModel):
    items: List[Any]
    total: int
    page: int
    size: int
    pages: int
