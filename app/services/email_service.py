from fastapi_mail import FastMail, MessageSchema, MessageType
from fastapi_mail.config import ConnectionConfig
from jinja2 import Template
from app.config.settings import settings

conf = ConnectionConfig(
    MAIL_USERNAME=settings.SMTP_USER,
    MAIL_PASSWORD=settings.SMTP_PASSWORD,
    MAIL_FROM=settings.SMTP_USER,
    MAIL_PORT=settings.SMTP_PORT,
    MAIL_SERVER=settings.SMTP_HOST,
    MAIL_STARTTLS=True,
    MAIL_SSL_TLS=False,
    USE_CREDENTIALS=True,
    VALIDATE_CERTS=True,
)

fastmail = FastMail(conf)


class EmailService:
    async def send_email(self, email_to: str, subject: str, template_name: str, context: dict):
        template_path = f"templates/email/{template_name}.html"
        with open(template_path) as f:
            template = Template(f.read())
        html_body = template.render(**context)
        message = MessageSchema(
            subject=subject,
            recipients=[email_to],
            body=html_body,
            subtype=MessageType.html,
        )
        await fastmail.send_message(message)

    async def send_activation_email(self, user_email: str, user_name: str, activation_code: str):
        await self.send_email(
            email_to=user_email,
            subject="Activate Your QuietSpace Account",
            template_name="activation",
            context={
                "username": user_name,
                "activation_code": activation_code,
                "activation_url": f"{settings.FRONTEND_URL}/activate?code={activation_code}",
            },
        )

    async def send_notification_email(self, user_email: str, user_name: str, notification_type: str, content: str):
        await self.send_email(
            email_to=user_email,
            subject=f"New Notification: {notification_type}",
            template_name="notification",
            context={
                "username": user_name,
                "notification_type": notification_type,
                "content": content,
            },
        )
