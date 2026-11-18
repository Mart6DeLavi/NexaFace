"""Face detection powered by MediaPipe Face Detector (Tasks API)."""

import threading
from functools import lru_cache

import cv2
import mediapipe as mp
import numpy as np
from mediapipe.tasks import python as mp_python
from mediapipe.tasks.python import vision

from app.config import settings
from app.schemas.detection import BoundingBox, DetectedFace


class FaceDetector:
    """Thin wrapper around the MediaPipe face detector.

    MediaPipe Tasks objects are not guaranteed to be thread-safe, while FastAPI
    runs synchronous handlers in a thread pool, so calls are serialized.
    """

    def __init__(self, model_path: str, min_detection_confidence: float) -> None:
        base_options = mp_python.BaseOptions(model_asset_path=model_path)
        options = vision.FaceDetectorOptions(
            base_options=base_options,
            running_mode=vision.RunningMode.IMAGE,
            min_detection_confidence=min_detection_confidence,
        )
        self._detector = vision.FaceDetector.create_from_options(options)
        self._lock = threading.Lock()

    def detect(self, image_bgr: np.ndarray) -> list[DetectedFace]:
        height, width = image_bgr.shape[:2]
        rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        mp_image = mp.Image(image_format=mp.ImageFormat.SRGB, data=rgb)

        with self._lock:
            result = self._detector.detect(mp_image)

        faces: list[DetectedFace] = []
        for detection in result.detections:
            box = detection.bounding_box
            x = max(0, box.origin_x)
            y = max(0, box.origin_y)
            faces.append(
                DetectedFace(
                    box=BoundingBox(
                        x=x,
                        y=y,
                        width=min(box.width, width - x),
                        height=min(box.height, height - y),
                    ),
                    confidence=round(float(detection.categories[0].score), 4),
                )
            )
        return faces


@lru_cache(maxsize=1)
def get_detector() -> FaceDetector:
    return FaceDetector(
        model_path=str(settings.detector_model_path),
        min_detection_confidence=settings.min_detection_confidence,
    )
