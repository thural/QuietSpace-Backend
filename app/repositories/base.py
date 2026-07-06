from typing import Generic, TypeVar, Optional, List
from uuid import UUID
from datetime import datetime
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, tuple_
from sqlalchemy.orm import selectinload, joinedload, Load
from app.models.base import BaseEntity
from app.utils.cursor import encode_cursor, decode_cursor

ModelType = TypeVar("ModelType", bound=BaseEntity)


class BaseRepository(Generic[ModelType]):
    """
    Generic repository with async database operations.

    Pagination Decision Matrix:
    ---------------------------
    Cursor-Based (use paginate_cursor):       | Offset-Based (use paginate_offset):
      - Social feeds (posts, notifications)   | - Static/admin data (user search)
      - Chat messages (chronological order)   | - Followers/following lists
      - Comment threads                       | - Blocked users list
      - Real-time data with new items         | - Admin panels, user management
      - Infinite scroll UIs                   | - Page-number-based UIs
    """

    def __init__(self, model: type[ModelType], session: AsyncSession):
        self.model = model
        self.session = session

    async def get(
        self, id: UUID, load_options: Optional[list[Load]] = None
    ) -> Optional[ModelType]:
        stmt = select(self.model).where(self.model.id == id)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_all(
        self, load_options: Optional[list[Load]] = None
    ) -> List[ModelType]:
        stmt = select(self.model)
        if load_options:
            stmt = stmt.options(*load_options)
        result = await self.session.execute(stmt)
        return result.scalars().all()

    async def create(self, obj: ModelType) -> ModelType:
        self.session.add(obj)
        await self.session.commit()
        await self.session.refresh(obj)
        return obj

    async def update(self, obj: ModelType) -> ModelType:
        await self.session.commit()
        await self.session.refresh(obj)
        return obj

    async def delete(self, id: UUID) -> bool:
        obj = await self.get(id)
        if obj:
            await self.session.delete(obj)
            await self.session.commit()
            return True
        return False

    async def paginate_cursor(
        self,
        stmt,
        cursor: str | None,
        limit: int = 20,
    ) -> tuple[list[ModelType], str | None, bool]:
        sort_column = self.model.created_at
        if cursor:
            cursor_ts, cursor_id = decode_cursor(cursor)
            stmt = stmt.where(
                tuple_(sort_column, self.model.id) < tuple_(cursor_ts, cursor_id)
            )
        stmt = stmt.order_by(sort_column.desc(), self.model.id.desc()).limit(limit + 1)
        result = await self.session.execute(stmt)
        items = list(result.scalars().all())
        has_more = len(items) > limit
        items = items[:limit]
        next_cursor = None
        if has_more and items:
            last = items[-1]
            next_cursor = encode_cursor(last.created_at, last.id)
        return items, next_cursor, has_more

    async def paginate_offset(
        self,
        stmt,
        page: int = 1,
        size: int = 20,
    ) -> tuple[list[ModelType], int]:
        count_stmt = select(func.count()).select_from(stmt.subquery())
        count_result = await self.session.execute(count_stmt)
        total = count_result.scalar_one()
        stmt = stmt.limit(size).offset((page - 1) * size)
        result = await self.session.execute(stmt)
        items = list(result.scalars().all())
        return items, total
