from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(tags=["health"])


class HealthResponse(BaseModel):
    status: str
    service: str


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    """Liveness endpoint used to verify that the face service is running."""
    return HealthResponse(status="UP", service="face-service")
