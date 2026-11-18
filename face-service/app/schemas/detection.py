"""Pydantic schemas for face detection."""

from pydantic import BaseModel, Field


class BoundingBox(BaseModel):
    x: int = Field(ge=0, description="Left edge in pixels")
    y: int = Field(ge=0, description="Top edge in pixels")
    width: int = Field(gt=0, description="Box width in pixels")
    height: int = Field(gt=0, description="Box height in pixels")


class DetectedFace(BaseModel):
    box: BoundingBox
    confidence: float = Field(ge=0.0, le=1.0)


class QualityIssue(BaseModel):
    code: str
    message: str


class ImageQuality(BaseModel):
    valid: bool
    issues: list[QualityIssue]
    blur_score: float
    brightness: float
    face_count: int


class DetectResponse(BaseModel):
    image_width: int
    image_height: int
    faces: list[DetectedFace]
    quality: ImageQuality
