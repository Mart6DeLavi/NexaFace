from pathlib import Path

import cv2
import numpy as np
import pytest
from fastapi.testclient import TestClient

from app.liveness.metrics import RIGHT_EYE, eye_aspect_ratio, head_turn_ratio
from main import app

client = TestClient(app)
FIXTURE = Path(__file__).parent / "fixtures" / "face.jpg"

SHEARED_RIGHT = (0.0, 0.3, 0.45)
SHEARED_LEFT = (0.0, -0.3, -0.45)


def frame_bytes(shear: float = 0.0) -> bytes:
    image = cv2.imread(str(FIXTURE))
    if shear:
        matrix = np.float32([[1, shear, 0], [0, 1, 0]])
        image = cv2.warpAffine(
            image, matrix, (image.shape[1], image.shape[0]),
            borderMode=cv2.BORDER_REPLICATE,
        )
    _, encoded = cv2.imencode(".jpg", image)
    return encoded.tobytes()


def post_liveness(challenge: str, frames: list[bytes]):
    files = [
        ("images", (f"frame{index}.jpg", data, "image/jpeg"))
        for index, data in enumerate(frames)
    ]
    return client.post(f"/face/liveness?challenge={challenge}", files=files)


def test_turn_right_passes_when_nose_moves_right() -> None:
    response = post_liveness("turn_right", [frame_bytes(s) for s in SHEARED_RIGHT])

    assert response.status_code == 200
    body = response.json()
    assert body["passed"] is True
    assert body["challenge"] == "turn_right"
    assert body["frames_analyzed"] == 3
    assert body["metric"] >= body["threshold"]


def test_turn_left_fails_for_right_turn_frames() -> None:
    response = post_liveness("turn_left", [frame_bytes(s) for s in SHEARED_RIGHT])

    assert response.status_code == 200
    assert response.json()["passed"] is False


def test_turn_left_passes_when_nose_moves_left() -> None:
    response = post_liveness("turn_left", [frame_bytes(s) for s in SHEARED_LEFT])

    assert response.status_code == 200
    body = response.json()
    assert body["passed"] is True
    assert body["metric"] <= -body["threshold"]


def test_open_eyes_do_not_pass_blink_challenge() -> None:
    response = post_liveness("blink", [frame_bytes()] * 3)

    assert response.status_code == 200
    body = response.json()
    assert body["passed"] is False
    assert body["threshold"] == pytest.approx(0.19)


def test_frame_count_is_validated() -> None:
    response = post_liveness("blink", [frame_bytes(), frame_bytes()])

    assert response.status_code == 400
    assert response.json()["error"] == "INVALID_FRAME_COUNT"


def test_sequence_without_face_is_rejected() -> None:
    blank = np.full((256, 256, 3), 128, dtype=np.uint8)
    _, encoded = cv2.imencode(".jpg", blank)

    response = post_liveness("turn_right", [frame_bytes(), encoded.tobytes(), frame_bytes()])

    assert response.status_code == 422
    assert response.json()["error"] == "NO_FACE_DETECTED"


def test_eye_aspect_ratio_drops_when_eye_closes() -> None:
    landmarks = np.zeros((478, 2), dtype=np.float32)
    landmarks[33] = (0.0, 0.0)
    landmarks[133] = (10.0, 0.0)
    landmarks[160] = (3.0, 2.0)
    landmarks[144] = (3.0, -2.0)
    landmarks[158] = (6.0, 2.0)
    landmarks[153] = (6.0, -2.0)

    open_ear = eye_aspect_ratio(landmarks, RIGHT_EYE)
    assert open_ear == pytest.approx(0.4)

    landmarks[[160, 158, 144, 153], 1] = 0.0
    assert eye_aspect_ratio(landmarks, RIGHT_EYE) == pytest.approx(0.0)


def test_head_turn_ratio_is_signed() -> None:
    landmarks = np.zeros((478, 2), dtype=np.float32)
    landmarks[33] = (0.0, 0.0)
    landmarks[263] = (10.0, 0.0)
    landmarks[1] = (5.0, 5.0)

    assert head_turn_ratio(landmarks) == pytest.approx(0.0)

    landmarks[1] = (7.0, 5.0)
    assert head_turn_ratio(landmarks) > 0.1

    landmarks[1] = (3.0, 5.0)
    assert head_turn_ratio(landmarks) < -0.1
