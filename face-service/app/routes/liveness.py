"""Basic challenge-response liveness checks based on MediaPipe landmarks."""

from fastapi import APIRouter, File, Query, UploadFile
from starlette.concurrency import run_in_threadpool

from app.config import settings
from app.detection.image_io import decode_image
from app.errors import FaceServiceError
from app.liveness.landmarker import get_landmarker
from app.liveness.metrics import eye_openness, head_turn_ratio
from app.schemas.liveness import LivenessChallenge, LivenessResponse

router = APIRouter(prefix="/face", tags=["face"])


async def _read_frame(image: UploadFile, index: int) -> bytes:
    data = await image.read(settings.max_image_bytes + 1)
    if len(data) > settings.max_image_bytes:
        raise FaceServiceError(
            413,
            "IMAGE_TOO_LARGE",
            f"Frame {index} exceeds the {settings.max_image_bytes} byte limit.",
        )
    return data


@router.post("/liveness", response_model=LivenessResponse)
async def check_liveness(
    challenge: LivenessChallenge = Query(...),
    images: list[UploadFile] = File(...),
) -> LivenessResponse:
    """Verify a head turn or a blink across a short frame sequence.

    This is a basic demonstration: it analyses landmark movement and does not
    protect against professional photo, video or deepfake attacks.
    """
    if not settings.liveness_min_frames <= len(images) <= settings.liveness_max_frames:
        raise FaceServiceError(
            400,
            "INVALID_FRAME_COUNT",
            f"Expected between {settings.liveness_min_frames} and "
            f"{settings.liveness_max_frames} frames, got {len(images)}.",
        )

    turn_ratios: list[float] = []
    openness: list[float] = []
    landmarker = get_landmarker()

    for index, upload in enumerate(images, start=1):
        frame = decode_image(await _read_frame(upload, index))
        faces = await run_in_threadpool(landmarker.detect, frame)

        if not faces:
            raise FaceServiceError(
                422,
                "NO_FACE_DETECTED",
                f"No face was found in frame {index}.",
            )
        if len(faces) > 1:
            raise FaceServiceError(
                422,
                "MULTIPLE_FACES",
                f"Frame {index} contains more than one face; only the user may be visible.",
            )

        turn_ratios.append(head_turn_ratio(faces[0]))
        openness.append(eye_openness(faces[0]))

    if challenge is LivenessChallenge.BLINK:
        metric = min(openness)
        threshold = settings.liveness_blink_threshold
        passed = metric <= threshold
        message = (
            "Blink detected."
            if passed
            else "No blink detected; keep the eyes open and blink once."
        )
    elif challenge is LivenessChallenge.TURN_LEFT:
        metric = min(turn_ratios)
        threshold = settings.liveness_turn_threshold
        passed = metric <= -threshold
        message = (
            "Head turn to the left detected."
            if passed
            else "Head turn to the left was not detected; turn the head further."
        )
    else:
        metric = max(turn_ratios)
        threshold = settings.liveness_turn_threshold
        passed = metric >= threshold
        message = (
            "Head turn to the right detected."
            if passed
            else "Head turn to the right was not detected; turn the head further."
        )

    return LivenessResponse(
        passed=passed,
        challenge=challenge,
        frames_analyzed=len(images),
        metric=round(metric, 4),
        threshold=threshold,
        message=message,
    )
