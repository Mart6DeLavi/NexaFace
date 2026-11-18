package com.nextface.face;

/**
 * Cosine similarity used for 1:1 face verification.
 */
final class EmbeddingMatcher {

    private EmbeddingMatcher() {
    }

    /**
     * Returns the cosine similarity in the range [-1, 1], or 0 when the vectors
     * are invalid (different dimensions, empty or zero-length).
     */
    static double cosineSimilarity(float[] first, float[] second) {
        if (first == null || second == null || first.length == 0 || first.length != second.length) {
            return 0.0;
        }

        double dot = 0.0;
        double firstNorm = 0.0;
        double secondNorm = 0.0;
        for (int i = 0; i < first.length; i++) {
            if (!Float.isFinite(first[i]) || !Float.isFinite(second[i])) {
                return 0.0;
            }
            dot += (double) first[i] * second[i];
            firstNorm += (double) first[i] * first[i];
            secondNorm += (double) second[i] * second[i];
        }

        if (firstNorm == 0.0 || secondNorm == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(firstNorm) * Math.sqrt(secondNorm));
    }
}
