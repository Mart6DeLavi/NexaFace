"""Face landmark extraction powered by MediaPipe Face Landmarker.

The liveness checks need dense landmarks (eye contours and nose tip), which the
lightweight face detector does not provide.
"""

import threading
from functools import lru_cache
from pathlib import Path

import cv2
import mediapipe as mp
import numpy as np
from mediapipe.tasks import python as mp_python
from mediapipe.tasks.python import vision

from app.config import settings
from app.errors import FaceServiceError


class FaceLandmarker:
    """Thread-safe wrapper around the MediaPipe Face Landmarker."""

    def __init__(self, model_path: str) -> None:
        if not Path(model_path).is_file():
            raise FaceServiceError(
                500,
                "MODEL_NOT_FOUND",
                f"Landmarker model not found at {model_path}.",
            )

        base_options = mp_python.BaseOptions(model_asset_path=model_path)
        options = vision.FaceLandmarkerOptions(
            base_options=base_options,
            running_mode=vision.RunningMode.IMAGE,
            num_faces=2,
        )
        self._landmarker = vision.FaceLandmarker.create_from_options(options)
        self._lock = threading.Lock()

    def detect(self, image_bgr: np.ndarray) -> list[np.ndarray]:
        """Return one (468, 2) pixel-coordinate array per detected face."""
        height, width = image_bgr.shape[:2]
        rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)

        with self._lock:
            result = self._landmarker.detect(mp_image)

        return [
            np.array(
                [[point.x * width, point.y * height] for point in face],
                dtype=np.float32,
            )
            for face in result.face_landmarks
        ]


@lru_cache(maxsize=1)
def get_landmarker() -> FaceLandmarker:
    return FaceLandmarker(str(settings.landmarker_model_path))
