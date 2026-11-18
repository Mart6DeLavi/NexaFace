"""NextFace Face Service.

Internal FastAPI application that performs face detection, embedding generation,
matching and liveness checks. It is called by the Spring Boot backend only and
never manages users or issues tokens.
"""

from fastapi import FastAPI

from app.routes import health

app = FastAPI(
    title="NextFace Face Service",
    description="Internal face analysis service for NextFace.",
    version="0.1.0",
    redoc_url=None,
)

app.include_router(health.router)
