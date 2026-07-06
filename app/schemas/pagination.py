from pydantic import BaseModel
from typing import Generic, TypeVar, Any

T = TypeVar("T")


class CursorParams(BaseModel):
    cursor: str | None = None
    limit: int = 20


class CursorResponse(BaseModel, Generic[T]):
    items: list[T]
    next_cursor: str | None = None
    has_more: bool = False


class OffsetParams(BaseModel):
    page: int = 1
    size: int = 20


class OffsetResponse(BaseModel, Generic[T]):
    items: list[T]
    total: int
    page: int
    size: int
    pages: int
