import secrets


def generate_activation_code() -> str:
    return secrets.token_urlsafe(16)
