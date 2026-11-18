package com.nextface.face;

/**
 * Thrown when the user has no biometric profile yet.
 */
public class FaceProfileNotFoundException extends RuntimeException {

    public FaceProfileNotFoundException() {
        super("No face profile is enrolled for this user");
    }
}
