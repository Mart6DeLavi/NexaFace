"""Face alignment for the SFace recognizer.

MediaPipe returns six keypoints; SFace expects a 112x112 crop aligned to the
standard ArcFace landmark layout. A similarity transform built from the eyes,
nose tip and mouth center is close enough for a prototype.
"""

import cv2
import numpy as np

from app.errors import FaceServiceError

OUTPUT_SIZE = 112

# Reference landmarks for a 112x112 ArcFace-style crop:
# right eye, left eye, nose tip, mouth center.
_REFERENCE = np.array(
    [
        [38.2946, 51.6963],
        [73.5318, 51.5014],
        [56.0252, 71.7366],
        [56.1354, 92.2851],
    ],
    dtype=np.float32,
)


def align_face(image_bgr: np.ndarray, landmarks: np.ndarray) -> np.ndarray:
    """Warp the face to the 112x112 reference layout used by SFace."""
    if landmarks is None or len(landmarks) < 4:
        raise FaceServiceError(
            422,
            "FACE_ALIGNMENT_FAILED",
            "The detected face does not contain enough landmarks for alignment.",
        )

    matrix, _ = cv2.estimateAffinePartial2D(
        landmarks[:4].astype(np.float32),
        _REFERENCE,
        method=cv2.LMEDS,
    )
    if matrix is None:
        raise FaceServiceError(
            422,
            "FACE_ALIGNMENT_FAILED",
            "The detected face landmarks are degenerate and cannot be aligned.",
        )

    return cv2.warpAffine(
        image_bgr,
        matrix,
        (OUTPUT_SIZE, OUTPUT_SIZE),
        flags=cv2.INTER_LINEAR,
        borderMode=cv2.BORDER_REPLICATE,
    )
