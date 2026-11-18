"""Tests for face embedding and comparison endpoints."""

from pathlib import Path

import numpy as np
from fastapi.testclient import TestClient

from main import app

client = TestClient(app)
FIXTURES = Path(__file__).parent / "fixtures"


def _embed(image_path: Path, filename: str) -> list[float]:
    with image_path.open("rb") as file:
        response = client.post(
            "/face/embedding",
            files={"image": (filename, file, "image/jpeg")},
        )
    assert response.status_code == 200, response.text
    return response.json()["embedding"]


def _compare(first: list[float], second: list[float]) -> dict:
    response = client.post(
        "/face/compare", json={"embedding1": first, "embedding2": second}
    )
    assert response.status_code == 200, response.text
    return response.json()


def test_embedding_returns_normalized_vector() -> None:
    with (FIXTURES / "face.jpg").open("rb") as file:
        response = client.post(
            "/face/embedding", files={"image": ("face.jpg", file, "image/jpeg")}
        )

    assert response.status_code == 200
    body = response.json()
    assert body["dimension"] == 128
    assert len(body["embedding"]) == 128
    assert abs(float(np.linalg.norm(body["embedding"])) - 1.0) < 1e-3
    assert body["quality"]["face_count"] == 1


def test_same_face_matches() -> None:
    first = _embed(FIXTURES / "face.jpg", "face.jpg")
    second = _embed(FIXTURES / "face.jpg", "face.jpg")

    result = _compare(first, second)

    assert result["similarity"] > 0.99
    assert result["match"] is True


def test_different_faces_do_not_match() -> None:
    first = _embed(FIXTURES / "face.jpg", "face.jpg")
    second = _embed(FIXTURES / "face2.jpg", "face2.jpg")

    result = _compare(first, second)

    assert result["similarity"] < result["threshold"]
    assert result["match"] is False


def test_compare_rejects_dimension_mismatch() -> None:
    response = client.post(
        "/face/compare", json={"embedding1": [1.0, 0.0], "embedding2": [1.0]}
    )

    assert response.status_code == 400
    assert response.json()["error"] == "EMBEDDING_DIMENSION_MISMATCH"


def test_compare_rejects_zero_vector() -> None:
    response = client.post(
        "/face/compare", json={"embedding1": [0.0, 0.0], "embedding2": [1.0, 0.0]}
    )

    assert response.status_code == 400
    assert response.json()["error"] == "INVALID_EMBEDDING"


def test_embedding_rejects_multiple_faces() -> None:
    import cv2

    image = cv2.imread(str(FIXTURES / "face.jpg"))
    doubled = cv2.hconcat([image, image])
    ok, encoded = cv2.imencode(".jpg", doubled)
    assert ok

    response = client.post(
        "/face/embedding",
        files={"image": ("two.jpg", encoded.tobytes(), "image/jpeg")},
    )

    assert response.status_code == 422
    assert response.json()["error"] == "MULTIPLE_FACES"


def test_embedding_without_face() -> None:
    import cv2

    blank = np.full((256, 256, 3), 255, dtype=np.uint8)
    ok, encoded = cv2.imencode(".jpg", blank)
    assert ok

    response = client.post(
        "/face/embedding",
        files={"image": ("blank.jpg", encoded.tobytes(), "image/jpeg")},
    )

    assert response.status_code == 422
    assert response.json()["error"] == "NO_FACE_DETECTED"
