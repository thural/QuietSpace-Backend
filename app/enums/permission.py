from enum import Enum


class Permission(str, Enum):
    READ = "READ"
    WRITE = "WRITE"
    DELETE = "DELETE"
    MANAGE_USERS = "MANAGE_USERS"
    MANAGE_POSTS = "MANAGE_POSTS"
    MANAGE_COMMENTS = "MANAGE_COMMENTS"
