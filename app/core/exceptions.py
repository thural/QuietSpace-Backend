from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse
from pydantic import ValidationError
from sqlmodel import SQLModel
from starlette.exceptions import HTTPException as StarletteHTTPException


class ErrorResponse(SQLModel):
    detail: str
    error_code: str
    field: str | None = None


def register_exception_handlers(app: FastAPI):
    @app.exception_handler(ValidationError)
    async def validation_exception_handler(request: Request, exc: ValidationError):
        return JSONResponse(
            status_code=422,
            content=ErrorResponse(
                detail="Validation failed",
                error_code="VALIDATION_ERROR",
                field=str(exc.errors()[0].get("loc", ["body"])[-1]),
            ).model_dump(),
        )

    @app.exception_handler(StarletteHTTPException)
    async def http_exception_handler(request: Request, exc: StarletteHTTPException):
        return JSONResponse(
            status_code=exc.status_code,
            content=ErrorResponse(
                detail=exc.detail,
                error_code=f"HTTP_{exc.status_code}",
            ).model_dump(),
        )

    @app.exception_handler(Exception)
    async def general_exception_handler(request: Request, exc: Exception):
        return JSONResponse(
            status_code=500,
            content=ErrorResponse(
                detail="Internal server error",
                error_code="INTERNAL_ERROR",
            ).model_dump(),
        )
