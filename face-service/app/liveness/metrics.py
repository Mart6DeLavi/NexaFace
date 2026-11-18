"""Pure landmark metrics used by the liveness checks.

Landmark indices follow the MediaPipe Face Mesh topology:
- 33 / 263: outer eye corners;
- 1: nose tip;
- 33, 160, 158, 133, 153, 144: right eye contour;
- 362, 385, 387, 263, 373, 380: left eye contour.
"""

import numpy as np

RIGHT_EYE = (33, 160, 158, 133, 153, 144)
LEFT_EYE = (362, 385, 387, 263, 373, 380)
RIGHT_EYE_OUTER = 33
LEFT_EYE_OUTER = 263
NOSE_TIP = 1


def head_turn_ratio(landmarks: np.ndarray) -> float:
    """Signed horizontal nose offset relative to the eye line.

    Near 0 for a frontal face; grows positive when the nose shifts towards the
    right side of the image and negative towards the left. Normalized by the
    interocular distance, so it does not depend on the face size.
    """
    right_eye = landmarks[RIGHT_EYE_OUTER]
    left_eye = landmarks[LEFT_EYE_OUTER]
    nose = landmarks[NOSE_TIP]

    eye_distance = float(np.linalg.norm(left_eye - right_eye))
    if eye_distance == 0.0:
        return 0.0

    eye_center_x = (right_eye[0] + left_eye[0]) / 2.0
    return float((nose[0] - eye_center_x) / eye_distance)


def eye_aspect_ratio(landmarks: np.ndarray, indices: tuple[int, ...]) -> float:
    """Eye Aspect Ratio (EAR): high for open eyes, close to 0 when closed."""
    p1, p2, p3, p4, p5, p6 = (landmarks[index] for index in indices)
    horizontal = float(np.linalg.norm(p1 - p4))
    if horizontal == 0.0:
        return 0.0
    vertical = float(np.linalg.norm(p2 - p6) + np.linalg.norm(p3 - p5))
    return vertical / (2.0 * horizontal)


def eye_openness(landmarks: np.ndarray) -> float:
    """Smallest EAR of both eyes for a single frame."""
    return min(
        eye_aspect_ratio(landmarks, RIGHT_EYE),
        eye_aspect_ratio(landmarks, LEFT_EYE),
    )
