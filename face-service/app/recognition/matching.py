"""Cosine similarity between face embeddings."""

from collections.abc import Sequence

import numpy as np

from app.config import settings
from app.errors import FaceServiceError


def _as_vector(values: Sequence[float], name: str) -> np.ndarray:
    vector = np.asarray(values, dtype=np.float64)
    if vector.ndim != 1 or vector.size == 0:
        raise FaceServiceError(
            400, "INVALID_EMBEDDING", f"{name} must be a non-empty one-dimensional array."
        )
    if not np.all(np.isfinite(vector)):
        raise FaceServiceError(
            400, "INVALID_EMBEDDING", f"{name} contains non-finite values."
        )
    if float(np.linalg.norm(vector)) == 0.0:
        raise FaceServiceError(
            400, "INVALID_EMBEDDING", f"{name} must not be a zero vector."
        )
    return vector


def compare_embeddings(
    first: Sequence[float], second: Sequence[float]
) -> tuple[float, bool]:
    """Return the cosine similarity and the match decision for two embeddings."""
    vector_a = _as_vector(first, "embedding1")
    vector_b = _as_vector(second, "embedding2")
    if vector_a.size != vector_b.size:
        raise FaceServiceError(
            400,
            "EMBEDDING_DIMENSION_MISMATCH",
            f"Embeddings have different dimensions ({vector_a.size} and {vector_b.size}).",
        )

    similarity = float(
        np.dot(vector_a, vector_b)
        / (np.linalg.norm(vector_a) * np.linalg.norm(vector_b))
    )
    similarity = round(similarity, 6)
    return similarity, similarity >= settings.match_threshold
