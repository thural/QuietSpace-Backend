from typing import Generic, TypeVar, Optional, List
from uuid import UUID
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload, joinedload, Load
from app.models.base import BaseEntity

ModelType = TypeVar("ModelType", bound=BaseEntity)


class BaseRepository(Generic[ModelType]):
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
