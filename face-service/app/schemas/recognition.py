"""Schemas for face embedding and comparison endpoints."""

from pydantic import BaseModel, Field

from app.schemas.detection import DetectedFace, ImageQuality


class EmbeddingResponse(BaseModel):
    embedding: list[float]
    dimension: int
    face: DetectedFace
    quality: ImageQuality


class CompareRequest(BaseModel):
    embedding1: list[float] = Field(min_length=1, max_length=4096)
    embedding2: list[float] = Field(min_length=1, max_length=4096)


class CompareResponse(BaseModel):
    similarity: float
    match: bool
    threshold: float
