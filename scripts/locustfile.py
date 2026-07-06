import uuid
import random
from locust import HttpUser, task, between, constant


class ApiUser(HttpUser):
    wait_time = between(1, 3)
    token: str | None = None
    username: str = ""

    def on_start(self):
        unique_id = uuid.uuid4().hex[:8]
        self.username = f"loadtest_{unique_id}"
        email = f"{self.username}@test.com"
        password = "TestPass123!"

        register_payload = {
            "username": self.username,
            "email": email,
            "password": password,
            "display_name": f"Load Test {unique_id}",
        }
        with self.client.post(
            "/api/v1/auth/register",
            json=register_payload,
            catch_response=True,
            name="register",
        ) as resp:
            if resp.status_code not in (200, 201):
                resp.failure(f"Register failed: {resp.status_code}")

        login_payload = {"username": self.username, "password": password}
        with self.client.post(
            "/api/v1/auth/login",
            json=login_payload,
            catch_response=True,
            name="login",
        ) as resp:
            if resp.status_code == 200:
                data = resp.json()
                self.token = data.get("access_token")
            else:
                resp.failure(f"Login failed: {resp.status_code}")

    @task(3)
    def get_me(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/users/me",
            headers={"Authorization": f"Bearer {self.token}"},
            name="get_me",
        )

    @task(3)
    def list_posts(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/posts",
            headers={"Authorization": f"Bearer {self.token}"},
            name="list_posts",
        )

    @task(2)
    def get_feed(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/posts?skip=0&limit=20",
            headers={"Authorization": f"Bearer {self.token}"},
            name="get_feed",
        )

    @task(1)
    def create_post(self):
        if not self.token:
            return
        payload = {
            "content": f"Load test post from {self.username} at {uuid.uuid4().hex[:6]}",
        }
        with self.client.post(
            "/api/v1/posts",
            json=payload,
            headers={"Authorization": f"Bearer {self.token}"},
            catch_response=True,
            name="create_post",
        ) as resp:
            if resp.status_code not in (200, 201):
                resp.failure(f"Create post failed: {resp.status_code}")

    @task(1)
    def get_notifications(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/notifications",
            headers={"Authorization": f"Bearer {self.token}"},
            name="get_notifications",
        )


class ReadOnlyUser(HttpUser):
    wait_time = between(2, 5)
    token: str | None = None
    username: str = ""

    def on_start(self):
        unique_id = uuid.uuid4().hex[:8]
        self.username = f"readonly_{unique_id}"
        email = f"{self.username}@test.com"
        password = "TestPass123!"

        self.client.post(
            "/api/v1/auth/register",
            json={
                "username": self.username,
                "email": email,
                "password": password,
                "display_name": f"Read Only {unique_id}",
            },
            name="register",
        )

        with self.client.post(
            "/api/v1/auth/login",
            json={"username": self.username, "password": password},
            catch_response=True,
            name="login",
        ) as resp:
            if resp.status_code == 200:
                self.token = resp.json().get("access_token")

    @task(5)
    def list_posts(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/posts",
            headers={"Authorization": f"Bearer {self.token}"},
            name="list_posts",
        )

    @task(3)
    def get_me(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/users/me",
            headers={"Authorization": f"Bearer {self.token}"},
            name="get_me",
        )

    @task(1)
    def get_notifications(self):
        if not self.token:
            return
        self.client.get(
            "/api/v1/notifications",
            headers={"Authorization": f"Bearer {self.token}"},
            name="get_notifications",
        )
