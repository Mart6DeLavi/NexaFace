package com.nextface.common;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import com.nextface.auth.EmailAlreadyUsedException;
import com.nextface.auth.InvalidCredentialsException;
import com.nextface.face.FaceAuthenticationFailedException;
import com.nextface.face.FaceImageRequiredException;
import com.nextface.face.FaceProfileAlreadyExistsException;
import com.nextface.face.FaceProfileNotFoundException;
import com.nextface.face.FaceQualityRejectedException;
import com.nextface.face.FaceServiceRejectedException;
import com.nextface.face.FaceServiceUnavailableException;
import com.nextface.face.TooManyLoginAttemptsException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyUsedException.class)
    public ResponseEntity<ApiError> handleEmailAlreadyUsed(EmailAlreadyUsedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("EMAIL_IN_USE", "An account with this email already exists"));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("INVALID_CREDENTIALS", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> details.putIfAbsent(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_ERROR", "Request validation failed", details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("INVALID_REQUEST", "Request body is missing or malformed"));
    }

    @ExceptionHandler(FaceProfileAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleFaceProfileExists(FaceProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("FACE_PROFILE_EXISTS", ex.getMessage()));
    }

    @ExceptionHandler(FaceAuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleFaceAuthenticationFailed(FaceAuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("FACE_AUTH_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ApiError> handleTooManyLoginAttempts(TooManyLoginAttemptsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiError("TOO_MANY_ATTEMPTS", ex.getMessage()));
    }

    @ExceptionHandler(FaceProfileNotFoundException.class)
    public ResponseEntity<ApiError> handleFaceProfileNotFound(FaceProfileNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("FACE_PROFILE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(FaceQualityRejectedException.class)
    public ResponseEntity<ApiError> handleFaceQuality(FaceQualityRejectedException ex) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("issues", String.join(", ", ex.getIssues()));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError("FACE_QUALITY_REJECTED", ex.getMessage(), details));
    }

    @ExceptionHandler(FaceServiceRejectedException.class)
    public ResponseEntity<ApiError> handleFaceServiceRejected(FaceServiceRejectedException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError("FACE_IMAGE_REJECTED", ex.getMessage()));
    }

    @ExceptionHandler(FaceServiceUnavailableException.class)
    public ResponseEntity<ApiError> handleFaceServiceUnavailable(FaceServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError("FACE_SERVICE_UNAVAILABLE", ex.getMessage()));
    }

    @ExceptionHandler({FaceImageRequiredException.class, MissingServletRequestPartException.class})
    public ResponseEntity<ApiError> handleFaceImageRequired(Exception ex) {
        return ResponseEntity.badRequest()
                .body(new ApiError("FACE_IMAGE_REQUIRED",
                        "A non-empty image part named 'image' is required"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiError("IMAGE_TOO_LARGE", "The uploaded image exceeds the size limit"));
    }
}
