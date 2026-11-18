"""NextFace Face Service.

Internal FastAPI application that performs face detection, embedding generation,
matching and liveness checks. It is called by the Spring Boot backend only and
never manages users or issues tokens.
"""

from fastapi import FastAPI

from app.errors import register_error_handlers
from app.routes import face, health

app = FastAPI(
    title="NextFace Face Service",
    description="Internal face analysis service for NextFace.",
    version="0.1.0",
    redoc_url=None,
)

register_error_handlers(app)
app.include_router(health.router)
app.include_router(face.router)
