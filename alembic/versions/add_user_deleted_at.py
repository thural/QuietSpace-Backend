"""add deleted_at column to user table for soft deletes

Revision ID: add_user_deleted_at
Revises: add_chat_deleted_at
Create Date: 2026-07-08 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
import sqlmodel


revision: str = "add_user_deleted_at"
down_revision: Union[str, None] = "add_chat_deleted_at"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("user", sa.Column("deleted_at", sa.DateTime(), nullable=True))
    op.create_index(op.f("ix_user_deleted_at"), "user", ["deleted_at"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_user_deleted_at"), table_name="user")
    op.drop_column("user", "deleted_at")
