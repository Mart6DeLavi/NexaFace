package com.nextface.face;

/**
 * Thrown when too many failed face login attempts were made in a short period.
 */
public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException() {
        super("Too many failed face login attempts, try again later");
    }
}
