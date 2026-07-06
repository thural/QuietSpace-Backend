from enum import Enum


class StatusType(str, Enum):
    PENDING = "PENDING"
    ACTIVE = "ACTIVE"
    BANNED = "BANNED"
