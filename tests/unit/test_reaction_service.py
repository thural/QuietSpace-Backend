import pytest
from unittest.mock import AsyncMock, MagicMock, patch
from uuid import UUID, uuid4


@pytest.mark.asyncio
async def test_toggle_reaction_create_new():
    from app.services.reaction_service import ReactionService
    from app.schemas.reaction import ReactionCreate

    user_id = uuid4()
    post_id = uuid4()

    reaction_repo = AsyncMock()
    reaction_repo.get_by_user_and_target.return_value = None
    session = MagicMock()

    with patch("app.services.reaction_service.ReactionRepository", return_value=reaction_repo):
        service = ReactionService(session)
        reaction_in = ReactionCreate(type="LIKE", post_id=post_id, comment_id=None)
        await service.toggle_reaction(user_id, reaction_in)
        reaction_repo.create.assert_awaited_once()


@pytest.mark.asyncio
async def test_toggle_reaction_delete_existing():
    from app.services.reaction_service import ReactionService
    from app.schemas.reaction import ReactionCreate

    user_id = uuid4()
    post_id = uuid4()
    existing = MagicMock()
    existing.id = uuid4()

    reaction_repo = AsyncMock()
    reaction_repo.get_by_user_and_target.return_value = existing
    session = MagicMock()

    with patch("app.services.reaction_service.ReactionRepository", return_value=reaction_repo):
        service = ReactionService(session)
        reaction_in = ReactionCreate(type="LIKE", post_id=post_id, comment_id=None)
        await service.toggle_reaction(user_id, reaction_in)
        reaction_repo.delete.assert_awaited_once_with(existing.id)


@pytest.mark.asyncio
async def test_get_reaction_count():
    from app.services.reaction_service import ReactionService

    reaction_repo = AsyncMock()
    reaction_repo.count_by_post.return_value = 5
    session = MagicMock()

    with patch("app.services.reaction_service.ReactionRepository", return_value=reaction_repo):
        service = ReactionService(session)
        count = await service.get_reaction_count(uuid4())
        assert count == 5
