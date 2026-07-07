"""add indexes on user firstname and lastname

Revision ID: add_user_name_indexes
Revises: add_poll_tables
Create Date: 2026-07-07 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
import sqlmodel


revision: str = "add_user_name_indexes"
down_revision: Union[str, None] = "add_poll_tables"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_index(op.f("ix_user_firstname"), "user", ["firstname"], unique=False)
    op.create_index(op.f("ix_user_lastname"), "user", ["lastname"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_user_firstname"), table_name="user")
    op.drop_index(op.f("ix_user_lastname"), table_name="user")
