from pathlib import Path

import cv2
import numpy as np
from fastapi.testclient import TestClient

from main import app

client = TestClient(app)
FIXTURE = Path(__file__).parent / "fixtures" / "face.jpg"


def post_image(data: bytes, filename: str = "face.jpg", content_type: str = "image/jpeg"):
    return client.post("/face/detect", files={"image": (filename, data, content_type)})


def test_detects_single_face_and_reports_quality() -> None:
    response = post_image(FIXTURE.read_bytes())

    assert response.status_code == 200
    body = response.json()
    assert body["image_width"] == 409
    assert body["image_height"] == 512
    assert len(body["faces"]) == 1
    assert 0.0 < body["faces"][0]["confidence"] <= 1.0
    assert body["faces"][0]["box"]["width"] > 0
    assert body["quality"]["face_count"] == 1
    assert body["quality"]["valid"] is True
    assert body["quality"]["issues"] == []


def test_multiple_faces_produce_quality_issue() -> None:
    image = cv2.imread(str(FIXTURE))
    doubled = np.hstack([image, image])
    _, encoded = cv2.imencode(".jpg", doubled)

    response = post_image(encoded.tobytes())

    assert response.status_code == 200
    body = response.json()
    assert body["quality"]["face_count"] == 2
    assert body["quality"]["valid"] is False
    assert [issue["code"] for issue in body["quality"]["issues"]] == ["MULTIPLE_FACES"]


def test_image_without_face_returns_error() -> None:
    blank = np.full((300, 300, 3), 127, dtype=np.uint8)
    _, encoded = cv2.imencode(".png", blank)

    response = post_image(encoded.tobytes(), filename="blank.png", content_type="image/png")

    assert response.status_code == 422
    assert response.json()["error"] == "NO_FACE_DETECTED"


def test_invalid_image_returns_error() -> None:
    response = post_image(b"definitely not an image")

    assert response.status_code == 400
    assert response.json()["error"] == "INVALID_IMAGE"


def test_empty_file_returns_error() -> None:
    response = post_image(b"")

    assert response.status_code == 400
    assert response.json()["error"] == "INVALID_IMAGE"


def test_oversized_image_returns_error() -> None:
    response = post_image(b"\0" * (5 * 1024 * 1024 + 1))

    assert response.status_code == 413
    assert response.json()["error"] == "IMAGE_TOO_LARGE"
