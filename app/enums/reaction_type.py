from enum import Enum


class ReactionType(str, Enum):
    LIKE = "LIKE"
    LOVE = "LOVE"
    HAHA = "HAHA"
    WOW = "WOW"
    SAD = "SAD"
    ANGRY = "ANGRY"
