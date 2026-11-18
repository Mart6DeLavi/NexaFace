"""Runtime configuration for the face service.

Values are read from environment variables so the service can be tuned without
code changes. Thresholds are explicit and configurable, as required by the plan.
"""

import os
from dataclasses import dataclass
from pathlib import Path

SERVICE_ROOT = Path(__file__).resolve().parents[1]


def _env_float(name: str, default: float) -> float:
    value = os.getenv(name)
    return float(value) if value else default


def _env_int(name: str, default: int) -> int:
    value = os.getenv(name)
    return int(value) if value else default


@dataclass(frozen=True)
class Settings:
    detector_model_path: Path
    recognition_model_path: Path
    landmarker_model_path: Path
    match_threshold: float
    liveness_turn_threshold: float
    liveness_blink_threshold: float
    liveness_min_frames: int
    liveness_max_frames: int
    min_detection_confidence: float
    min_face_size_px: int
    blur_threshold: float
    brightness_min: float
    brightness_max: float
    max_image_bytes: int

    @classmethod
    def from_env(cls) -> "Settings":
        model_path = Path(
            os.getenv(
                "FACE_DETECTOR_MODEL",
                str(SERVICE_ROOT / "models" / "blaze_face_short_range.tflite"),
            )
        )
        recognition_model_path = Path(
            os.getenv(
                "FACE_RECOGNITION_MODEL",
                str(SERVICE_ROOT / "models" / "face_recognition_sface_2021dec.onnx"),
            )
        )
        landmarker_model_path = Path(
            os.getenv(
                "FACE_LANDMARKER_MODEL",
                str(SERVICE_ROOT / "models" / "face_landmarker.task"),
            )
        )
        return cls(
            detector_model_path=model_path,
            recognition_model_path=recognition_model_path,
            landmarker_model_path=landmarker_model_path,
            match_threshold=_env_float("FACE_MATCH_THRESHOLD", 0.363),
            liveness_turn_threshold=_env_float("FACE_LIVENESS_TURN_THRESHOLD", 0.08),
            liveness_blink_threshold=_env_float("FACE_LIVENESS_BLINK_THRESHOLD", 0.19),
            liveness_min_frames=_env_int("FACE_LIVENESS_MIN_FRAMES", 3),
            liveness_max_frames=_env_int("FACE_LIVENESS_MAX_FRAMES", 20),
            min_detection_confidence=_env_float("FACE_MIN_DETECTION_CONFIDENCE", 0.5),
            min_face_size_px=_env_int("FACE_MIN_SIZE_PX", 80),
            blur_threshold=_env_float("FACE_BLUR_THRESHOLD", 60.0),
            brightness_min=_env_float("FACE_BRIGHTNESS_MIN", 40.0),
            brightness_max=_env_float("FACE_BRIGHTNESS_MAX", 220.0),
            max_image_bytes=_env_int("FACE_MAX_IMAGE_BYTES", 5 * 1024 * 1024),
        )


settings = Settings.from_env()
