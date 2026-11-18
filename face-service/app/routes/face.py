"""Face analysis endpoints."""

from fastapi import APIRouter, File, UploadFile
from starlette.concurrency import run_in_threadpool

from app.config import settings
from app.detection.detector import get_detector
from app.detection.image_io import decode_image
from app.detection.quality import assess_quality
from app.errors import FaceServiceError
from app.schemas.detection import DetectResponse

router = APIRouter(prefix="/face", tags=["face"])


@router.post("/detect", response_model=DetectResponse)
async def detect_face(image: UploadFile = File(...)) -> DetectResponse:
    """Detect faces in the image and report basic quality metrics."""
    data = await image.read(settings.max_image_bytes + 1)
    if len(data) > settings.max_image_bytes:
        raise FaceServiceError(
            413,
            "IMAGE_TOO_LARGE",
            f"The image exceeds the {settings.max_image_bytes} byte limit.",
        )

    image_bgr = decode_image(data)
    faces = await run_in_threadpool(get_detector().detect, image_bgr)

    if not faces:
        raise FaceServiceError(
            422,
            "NO_FACE_DETECTED",
            "No face was found in the image; make sure the face is visible and well lit.",
        )

    height, width = image_bgr.shape[:2]
    return DetectResponse(
        image_width=width,
        image_height=height,
        faces=faces,
        quality=assess_quality(image_bgr, faces),
    )
