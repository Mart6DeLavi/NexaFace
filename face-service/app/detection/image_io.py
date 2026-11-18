"""Image decoding helpers."""

import cv2
import numpy as np

from app.errors import FaceServiceError


def decode_image(data: bytes) -> np.ndarray:
    if not data:
        raise FaceServiceError(400, "INVALID_IMAGE", "The uploaded file is empty.")

    buffer = np.frombuffer(data, dtype=np.uint8)
    image = cv2.imdecode(buffer, cv2.IMREAD_COLOR)
    if image is None:
        raise FaceServiceError(
            400,
            "INVALID_IMAGE",
            "The uploaded file is not a supported image (JPEG or PNG expected).",
        )
    return image
