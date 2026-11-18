"""Domain errors for the face service."""

from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse


class FaceServiceError(Exception):
    """Error that is rendered as a stable JSON payload with an error code."""

    def __init__(self, status_code: int, code: str, message: str) -> None:
        super().__init__(message)
        self.status_code = status_code
        self.code = code
        self.message = message


def register_error_handlers(app: FastAPI) -> None:
    @app.exception_handler(FaceServiceError)
    async def face_service_error_handler(_: Request, exc: FaceServiceError) -> JSONResponse:
        return JSONResponse(
            status_code=exc.status_code,
            content={"error": exc.code, "message": exc.message},
        )
