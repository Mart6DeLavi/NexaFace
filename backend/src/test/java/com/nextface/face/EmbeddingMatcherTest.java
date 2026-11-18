package com.nextface.face;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmbeddingMatcherTest {

    @Test
    void identicalVectorsHaveSimilarityOne() {
        float[] vector = {0.1f, 0.2f, 0.3f};

        assertThat(EmbeddingMatcher.cosineSimilarity(vector, vector)).isCloseTo(1.0, within());
    }

    @Test
    void orthogonalVectorsHaveSimilarityZero() {
        assertThat(EmbeddingMatcher.cosineSimilarity(
                new float[] {1f, 0f}, new float[] {0f, 1f})).isCloseTo(0.0, within());
    }

    @Test
    void oppositeVectorsHaveSimilarityMinusOne() {
        assertThat(EmbeddingMatcher.cosineSimilarity(
                new float[] {1f, 2f}, new float[] {-1f, -2f})).isCloseTo(-1.0, within());
    }

    @Test
    void invalidVectorsHaveSimilarityZero() {
        assertThat(EmbeddingMatcher.cosineSimilarity(new float[] {1f}, new float[] {1f, 2f})).isZero();
        assertThat(EmbeddingMatcher.cosineSimilarity(new float[] {0f, 0f}, new float[] {1f, 2f})).isZero();
        assertThat(EmbeddingMatcher.cosineSimilarity(new float[] {Float.NaN, 1f}, new float[] {1f, 2f}))
                .isZero();
        assertThat(EmbeddingMatcher.cosineSimilarity(null, new float[] {1f})).isZero();
    }

    private static org.assertj.core.data.Offset<Double> within() {
        return org.assertj.core.data.Offset.offset(1e-6);
    }
}
