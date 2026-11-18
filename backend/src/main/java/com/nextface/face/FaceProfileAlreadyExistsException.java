package com.nextface.face;

/**
 * Thrown when a user already has a face profile and has not confirmed replacement.
 */
public class FaceProfileAlreadyExistsException extends RuntimeException {

    public FaceProfileAlreadyExistsException() {
        super("A face profile already exists for this user");
    }
}
