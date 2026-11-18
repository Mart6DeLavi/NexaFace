package com.nextface.face;

/**
 * Thrown when the image itself is missing or empty.
 */
public class FaceImageRequiredException extends RuntimeException {

    public FaceImageRequiredException() {
        super("A non-empty image part named 'image' is required");
    }
}
