"""Face analysis endpoints."""

from fastapi import APIRouter, File, UploadFile
from starlette.concurrency import run_in_threadpool

from app.config import settings
from app.detection.detector import get_detector
from app.detection.image_io import decode_image
from app.detection.quality import assess_quality
from app.errors import FaceServiceError
from app.recognition.align import align_face
from app.recognition.embedder import get_embedder
from app.recognition.matching import compare_embeddings
from app.schemas.detection import DetectResponse
from app.schemas.recognition import CompareRequest, CompareResponse, EmbeddingResponse

router = APIRouter(prefix="/face", tags=["face"])


async def _read_image(image: UploadFile) -> bytes:
    data = await image.read(settings.max_image_bytes + 1)
    if len(data) > settings.max_image_bytes:
        raise FaceServiceError(
            413,
            "IMAGE_TOO_LARGE",
            f"The image exceeds the {settings.max_image_bytes} byte limit.",
        )
    return data


@router.post("/detect", response_model=DetectResponse)
async def detect_face(image: UploadFile = File(...)) -> DetectResponse:
    """Detect faces in the image and report basic quality metrics."""
    data = await _read_image(image)
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


@router.post("/embedding", response_model=EmbeddingResponse)
async def create_embedding(image: UploadFile = File(...)) -> EmbeddingResponse:
    """Generate a normalized embedding for the single face in the image."""
    data = await _read_image(image)
    image_bgr = decode_image(data)
    detections = await run_in_threadpool(
        get_detector().detect_with_landmarks, image_bgr
    )

    if not detections:
        raise FaceServiceError(
            422,
            "NO_FACE_DETECTED",
            "No face was found in the image; make sure the face is visible and well lit.",
        )
    if len(detections) > 1:
        raise FaceServiceError(
            422,
            "MULTIPLE_FACES",
            "Exactly one face is required; crop the image to a single person.",
        )

    detection = detections[0]
    aligned = align_face(image_bgr, detection.landmarks)
    embedder = get_embedder()
    embedding = await run_in_threadpool(embedder.embed, aligned)

    return EmbeddingResponse(
        embedding=[round(float(value), 6) for value in embedding],
        dimension=int(embedding.size),
        face=detection.face,
        quality=assess_quality(image_bgr, [detection.face]),
    )


@router.post("/compare", response_model=CompareResponse)
async def compare_faces(request: CompareRequest) -> CompareResponse:
    """Compare two embeddings using cosine similarity."""
    similarity, match = compare_embeddings(request.embedding1, request.embedding2)
    return CompareResponse(
        similarity=similarity,
        match=match,
        threshold=settings.match_threshold,
    )

