import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

transport = ASGITransport(app=app, raise_app_exceptions=False)


@pytest.mark.asyncio
async def test_asyncapi_spec_returns_json():
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/asyncapi.json")

    assert response.status_code == 200
    assert response.headers["content-type"] == "application/json"

    spec = response.json()
    assert spec["asyncapi"] == "2.6.0"
    assert spec["info"]["title"] == "QuietSpace Socket.IO API"
    assert "servers" in spec
    assert "channels" in spec
    assert "components" in spec


@pytest.mark.asyncio
async def test_asyncapi_spec_channels():
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/asyncapi.json")

    spec = response.json()
    channels = spec["channels"]

    # Client-to-server (publish) events
    assert "connect" in channels
    assert "disconnect" in channels
    assert "join_chat" in channels
    assert "leave_chat" in channels
    assert "send_message" in channels
    assert "delete_message" in channels
    assert "seen_message" in channels
    assert "set_online_status" in channels
    assert "get_online_users" in channels
    assert "public_message" in channels

    # Server-to-client (subscribe) events
    assert "user_connected" in channels
    assert "user_disconnected" in channels
    assert "new_message" in channels
    assert "message_in_chat" in channels
    assert "chat_event" in channels
    assert "online_users" in channels
    assert "user_status" in channels
    assert "typing_status" in channels
    assert "notification" in channels
    assert "unread_count" in channels
    assert "new_notification" in channels
    assert "system" in channels


@pytest.mark.asyncio
async def test_asyncapi_spec_has_publish_and_subscribe():
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/asyncapi.json")

    channels = response.json()["channels"]

    # Pure publish (client sends)
    assert "publish" in channels["join_chat"]
    assert "publish" in channels["leave_chat"]
    assert "publish" in channels["send_message"]
    assert "publish" in channels["delete_message"]
    assert "publish" in channels["seen_message"]
    assert "publish" in channels["set_online_status"]
    assert "publish" in channels["get_online_users"]

    # Pure subscribe (client receives)
    assert "subscribe" in channels["user_connected"]
    assert "subscribe" in channels["user_disconnected"]
    assert "subscribe" in channels["new_message"]
    assert "subscribe" in channels["message_in_chat"]
    assert "subscribe" in channels["chat_event"]
    assert "subscribe" in channels["online_users"]
    assert "subscribe" in channels["user_status"]
    assert "subscribe" in channels["typing_status"]
    assert "subscribe" in channels["notification"]
    assert "subscribe" in channels["unread_count"]
    assert "subscribe" in channels["new_notification"]
    assert "subscribe" in channels["system"]

    # Bidirectional (both publish and subscribe)
    assert "publish" in channels["public_message"]
    assert "subscribe" in channels["public_message"]

    # Lifecycle
    assert "subscribe" in channels["connect"]


@pytest.mark.asyncio
async def test_asyncapi_spec_components():
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/asyncapi.json")

    schemas = response.json()["components"]["schemas"]
    assert "UUIDStr" in schemas
    assert "Message" in schemas
    assert "Notification" in schemas
    assert "BaseEvent" in schemas
    assert "ChatEvent" in schemas
    assert "NotificationEvent" in schemas
    assert "SystemEvent" in schemas


@pytest.mark.asyncio
async def test_asyncapi_ui_returns_html():
    async with AsyncClient(transport=transport, base_url="http://test") as client:
        response = await client.get("/asyncapi")

    assert response.status_code == 200
    assert "text/html" in response.headers["content-type"]
    assert "AsyncApiStandalone" in response.text


def test_asyncapi_yaml_is_valid():
    from pathlib import Path
    from yaml import safe_load

    spec_path = Path("app/api/websocket/asyncapi.yaml")
    assert spec_path.exists()

    with open(spec_path) as f:
        spec = safe_load(f)

    assert spec["asyncapi"] == "2.6.0"
    assert len(spec["channels"]) >= 20
    assert len(spec["components"]["schemas"]) >= 7
