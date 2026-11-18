package com.nextface.face;

/**
 * Thrown when the face service cannot be reached or returns a server error.
 */
public class FaceServiceUnavailableException extends RuntimeException {

    public FaceServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public FaceServiceUnavailableException(String message) {
        super(message);
    }
}
