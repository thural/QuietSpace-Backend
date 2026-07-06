from enum import Enum


class EntityType(str, Enum):
    USER = "USER"
    POST = "POST"
    COMMENT = "COMMENT"
    CHAT = "CHAT"
    MESSAGE = "MESSAGE"
    REACTION = "REACTION"
    NOTIFICATION = "NOTIFICATION"
