package com.nextface.face;

import java.util.List;

/**
 * Thrown when the face service rejects an image because of quality issues.
 */
public class FaceQualityRejectedException extends RuntimeException {

    private final List<String> issues;

    public FaceQualityRejectedException(List<String> issues) {
        super("The image did not pass the quality checks");
        this.issues = List.copyOf(issues);
    }

    public List<String> getIssues() {
        return issues;
    }
}
