package com.nextface.face;

/**
 * Thrown when the face does not match the stored biometric profile. The message
 * is intentionally generic so it does not reveal enrollment details.
 */
public class FaceAuthenticationFailedException extends RuntimeException {

    public FaceAuthenticationFailedException() {
        super("Face authentication failed");
    }
}
