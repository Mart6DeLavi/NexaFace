package com.nextface.face;

/**
 * Thrown when the face service rejects the image (invalid image, no face, multiple faces).
 */
public class FaceServiceRejectedException extends RuntimeException {

    public FaceServiceRejectedException(String message) {
        super(message);
    }
}
