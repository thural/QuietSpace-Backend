from enum import Enum


class Role(str, Enum):
    USER = "USER"
    ADMIN = "ADMIN"


class StatusType(str, Enum):
    PENDING = "PENDING"
    ACTIVE = "ACTIVE"
    BANNED = "BANNED"
