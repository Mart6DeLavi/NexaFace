"""Image quality checks used before enrollment and authentication."""

import cv2
import numpy as np

from app.config import settings
from app.schemas.detection import DetectedFace, ImageQuality, QualityIssue


def assess_quality(image_bgr: np.ndarray, faces: list[DetectedFace]) -> ImageQuality:
    gray = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2GRAY)
    blur_score = float(cv2.Laplacian(gray, cv2.CV_64F).var())
    brightness = float(gray.mean())

    issues: list[QualityIssue] = []

    if blur_score < settings.blur_threshold:
        issues.append(
            QualityIssue(
                code="BLURRY",
                message="The image is too blurry; hold the camera steady and try again.",
            )
        )

    if brightness < settings.brightness_min:
        issues.append(
            QualityIssue(code="TOO_DARK", message="The image is too dark; add more light.")
        )
    elif brightness > settings.brightness_max:
        issues.append(
            QualityIssue(code="TOO_BRIGHT", message="The image is overexposed; reduce the light.")
        )

    if len(faces) > 1:
        issues.append(
            QualityIssue(
                code="MULTIPLE_FACES",
                message="More than one face is visible; only one person should be in the frame.",
            )
        )

    for face in faces:
        if min(face.box.width, face.box.height) < settings.min_face_size_px:
            issues.append(
                QualityIssue(
                    code="FACE_TOO_SMALL",
                    message="The face is too small; move closer to the camera.",
                )
            )

    return ImageQuality(
        valid=not issues,
        issues=issues,
        blur_score=round(blur_score, 2),
        brightness=round(brightness, 2),
        face_count=len(faces),
    )
