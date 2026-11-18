package com.nextface.face;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.client.ClientHttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Internal HTTP client for the Python face service.
 */
@Component
public class FaceServiceClient {

    private static final int EXPECTED_DIMENSION = 128;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public FaceServiceClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${nextface.face-service.base-url}") String baseUrl,
            @Value("${nextface.face-service.connect-timeout}") Duration connectTimeout,
            @Value("${nextface.face-service.read-timeout}") Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) connectTimeout.toMillis());
        requestFactory.setReadTimeout((int) readTimeout.toMillis());

        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.objectMapper = objectMapper;
    }

    /**
     * Sends an image to the face service and returns a 128-dimensional embedding.
     */
    public float[] createEmbedding(MultipartFile image) {
        byte[] bytes;
        try {
            bytes = image.getBytes();
        } catch (IOException ex) {
            throw new FaceServiceUnavailableException("Could not read the uploaded image", ex);
        }
        if (bytes.length == 0) {
            throw new FaceImageRequiredException();
        }

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("image", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "face.jpg";
            }
        }).contentType(resolveMediaType(image.getContentType()));

        FaceEmbeddingResponse response;
        try {
            response = restClient.post()
                    .uri("/face/embedding")
                    .body(body.build())
                    .retrieve()
                    .onStatus(status -> status.value() >= 400 && status.value() < 500,
                            (request, result) -> {
                                throw new FaceServiceRejectedException(readErrorMessage(result));
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (request, result) -> {
                                throw new FaceServiceUnavailableException(
                                        "The face service failed to process the image");
                            })
                    .body(FaceEmbeddingResponse.class);
        } catch (RestClientException ex) {
            throw new FaceServiceUnavailableException("The face service is unavailable", ex);
        }

        if (response == null || response.embedding() == null) {
            throw new FaceServiceUnavailableException("The face service returned an empty response");
        }

        rejectLowQuality(response.quality());

        int dimension = response.dimension() != null
                ? response.dimension()
                : response.embedding().size();
        if (dimension != EXPECTED_DIMENSION || response.embedding().size() != EXPECTED_DIMENSION) {
            throw new FaceServiceUnavailableException(
                    "The face service returned an unexpected embedding dimension");
        }

        float[] embedding = new float[EXPECTED_DIMENSION];
        for (int i = 0; i < EXPECTED_DIMENSION; i++) {
            Double value = response.embedding().get(i);
            if (value == null || !Double.isFinite(value)) {
                throw new FaceServiceUnavailableException(
                        "The face service returned an invalid embedding");
            }
            embedding[i] = value.floatValue();
        }
        return embedding;
    }

    private void rejectLowQuality(Quality quality) {
        if (quality == null || quality.valid()) {
            return;
        }
        List<String> issues = quality.issues() == null
                ? List.of()
                : quality.issues().stream().map(QualityIssue::code).toList();
        throw new FaceQualityRejectedException(issues.isEmpty() ? List.of("UNKNOWN") : issues);
    }

    private MediaType resolveMediaType(String contentType) {
        if (StringUtils.hasText(contentType)) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                if (mediaType.getType().equals("image")) {
                    return mediaType;
                }
            } catch (IllegalArgumentException ignored) {
                // fall through to the default
            }
        }
        return MediaType.IMAGE_JPEG;
    }

    private String readErrorMessage(ClientHttpResponse result) {
        try {
            JsonNode node = objectMapper.readTree(result.getBody());
            String message = node.path("message").asText(null);
            if (StringUtils.hasText(message)) {
                return message;
            }
            int statusCode = result.getStatusCode().value();
            HttpStatus status = HttpStatus.resolve(statusCode);
            return "The face service rejected the image"
                    + (status != null ? " (" + status.getReasonPhrase() + ")" : " (" + statusCode + ")");
        } catch (IOException ignored) {
            return "The face service rejected the image";
        }
    }

    record FaceEmbeddingResponse(List<Double> embedding, Integer dimension, Quality quality) {
    }

    record Quality(boolean valid, List<QualityIssue> issues) {
    }

    record QualityIssue(String code, String message) {
    }
}
