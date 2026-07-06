from enum import Enum


class EventType(str, Enum):
    CREATED = "CREATED"
    UPDATED = "UPDATED"
    DELETED = "DELETED"
    LIKED = "LIKED"
    COMMENTED = "COMMENTED"
    FOLLOWED = "FOLLOWED"
    MESSAGED = "MESSAGED"
    BANNED = "BANNED"
    ACTIVATED = "ACTIVATED"
