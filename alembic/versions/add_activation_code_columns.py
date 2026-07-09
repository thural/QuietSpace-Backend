"""add activation_code and activation_code_expires_at to user table

Revision ID: add_activation_code_columns
Revises: add_user_name_indexes
Create Date: 2026-07-07 00:00:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
import sqlmodel


revision: str = "add_activation_code_columns"
down_revision: Union[str, None] = "add_user_name_indexes"
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.add_column("user", sa.Column("activation_code", sa.String(length=255), nullable=True))
    op.add_column("user", sa.Column("activation_code_expires_at", sa.DateTime(timezone=True), nullable=True))


def downgrade() -> None:
    op.drop_column("user", "activation_code_expires_at")
    op.drop_column("user", "activation_code")
