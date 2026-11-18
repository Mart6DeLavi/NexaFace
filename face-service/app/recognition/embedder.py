"""SFace embedding generation via ONNX Runtime."""

import threading
from functools import lru_cache
from pathlib import Path

import cv2
import numpy as np
import onnxruntime as ort

from app.config import settings
from app.errors import FaceServiceError


class FaceEmbedder:
    """Thread-safe wrapper around the SFace ONNX model.

    The model expects a 112x112 RGB image with raw 0-255 pixel values and
    produces a 128-dimensional descriptor. Embeddings are L2-normalized so the
    cosine similarity is a simple dot product.
    """

    def __init__(self, model_path: str) -> None:
        if not Path(model_path).is_file():
            raise FaceServiceError(
                500,
                "MODEL_NOT_FOUND",
                f"Recognition model not found at {model_path}.",
            )

        ort.set_default_logger_severity(3)
        self._session = ort.InferenceSession(
            model_path, providers=["CPUExecutionProvider"]
        )
        self._input_name = self._session.get_inputs()[0].name
        self._lock = threading.Lock()

    @property
    def dimension(self) -> int:
        return int(self._session.get_outputs()[0].shape[1])

    def embed(self, aligned_bgr: np.ndarray) -> np.ndarray:
        rgb = cv2.cvtColor(aligned_bgr, cv2.COLOR_BGR2RGB)
        blob = np.ascontiguousarray(
            np.transpose(rgb.astype(np.float32), (2, 0, 1))[np.newaxis, ...]
        )

        with self._lock:
            output = self._session.run(None, {self._input_name: blob})[0][0]

        vector = np.asarray(output, dtype=np.float32)
        norm = float(np.linalg.norm(vector))
        if norm == 0.0:
            raise FaceServiceError(
                500,
                "EMBEDDING_FAILED",
                "The recognition model produced an empty embedding.",
            )
        return vector / norm


@lru_cache(maxsize=1)
def get_embedder() -> FaceEmbedder:
    return FaceEmbedder(str(settings.recognition_model_path))
