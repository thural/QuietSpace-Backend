from enum import Enum


class EmailTemplateName(str, Enum):
    ACTIVATION = "activation"
    NOTIFICATION = "notification"
