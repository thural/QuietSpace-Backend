from enum import Enum


class NotificationType(str, Enum):
    LIKE = "LIKE"
    COMMENT = "COMMENT"
    FOLLOW = "FOLLOW"
    MESSAGE = "MESSAGE"
    SYSTEM = "SYSTEM"
