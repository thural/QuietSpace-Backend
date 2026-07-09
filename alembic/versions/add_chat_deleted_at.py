"""add deleted_at column to chat table for soft deletes

Revision ID: add_chat_deleted_at
Revises: add_message_deleted_at
Create Date: 2026-07-08 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
import sqlmodel


revision: str = "add_chat_deleted_at"
down_revision: Union[str, None] = "add_message_deleted_at"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("chat", sa.Column("deleted_at", sa.DateTime(timezone=True), nullable=True))
    op.create_index(op.f("ix_chat_deleted_at"), "chat", ["deleted_at"], unique=False)


def downgrade() -> None:
    op.drop_index(op.f("ix_chat_deleted_at"), table_name="chat")
    op.drop_column("chat", "deleted_at")
