package com.nextface.face;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.nextface.audit.AuditEventType;
import com.nextface.audit.AuditService;
import com.nextface.auth.AuthResponse;
import com.nextface.auth.JwtService;
import com.nextface.user.User;
import com.nextface.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class FaceAuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FaceProfileRepository faceProfileRepository;

    @Mock
    private FaceServiceClient faceServiceClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private AuditService auditService;

    private FaceAuthenticationService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User("user@example.com", "hash");
    private final MockMultipartFile image =
            new MockMultipartFile("image", "face.jpg", "image/jpeg", new byte[] {1, 2, 3});

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(user, "id", userId);
        service = new FaceAuthenticationService(
                userRepository,
                faceProfileRepository,
                faceServiceClient,
                jwtService,
                loginAttemptService,
                auditService,
                0.363);
    }

    @Test
    void loginReturnsJwtAndAuditsSuccess() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new FaceProfile(user, vector(0.5f))));
        when(faceServiceClient.createEmbedding(any())).thenReturn(vector(0.5f));
        when(jwtService.generateToken(userId, "user@example.com")).thenReturn("token");
        when(jwtService.expirationSeconds()).thenReturn(3600L);

        AuthResponse response = service.login(" User@Example.com ", image);

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().email()).isEqualTo("user@example.com");
        verify(loginAttemptService).reset("user@example.com");
        verify(auditService).record(userId, AuditEventType.FACE_LOGIN, true);
    }

    @Test
    void loginRecordsFailureForUnknownEmail() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("unknown@example.com", image))
                .isInstanceOf(FaceAuthenticationFailedException.class);

        verify(loginAttemptService).recordFailure("unknown@example.com");
        verify(auditService).record(isNull(), eq(AuditEventType.FACE_LOGIN), eq(false));
        verifyNoInteractions(faceServiceClient);
    }

    @Test
    void loginRejectsDisabledUser() {
        user.setEnabled(false);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login("user@example.com", image))
                .isInstanceOf(FaceAuthenticationFailedException.class);

        verify(loginAttemptService).recordFailure("user@example.com");
        verifyNoInteractions(faceServiceClient);
    }

    @Test
    void loginFailsWhenProfileIsMissing() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("user@example.com", image))
                .isInstanceOf(FaceProfileNotFoundException.class);

        verifyNoInteractions(faceServiceClient);
        verifyNoInteractions(auditService);
    }

    @Test
    void loginRejectsMismatchedFaceAndCountsAttempt() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new FaceProfile(user, new float[] {1f, 0f})));
        when(faceServiceClient.createEmbedding(any())).thenReturn(new float[] {0f, 1f});

        assertThatThrownBy(() -> service.login("user@example.com", image))
                .isInstanceOf(FaceAuthenticationFailedException.class);

        verify(loginAttemptService).recordFailure("user@example.com");
        verify(auditService).record(userId, AuditEventType.FACE_LOGIN, false);
        verify(loginAttemptService, never()).reset(anyString());
    }

    @Test
    void loginStopsWhenAttemptsAreExhausted() {
        doThrow(new TooManyLoginAttemptsException())
                .when(loginAttemptService).assertAllowed("user@example.com");

        assertThatThrownBy(() -> service.login("user@example.com", image))
                .isInstanceOf(TooManyLoginAttemptsException.class);

        verifyNoInteractions(userRepository, faceServiceClient);
    }

    @Test
    void qualityRejectionDoesNotCountAsFailedAttempt() {
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new FaceProfile(user, vector(0.5f))));
        when(faceServiceClient.createEmbedding(any()))
                .thenThrow(new FaceQualityRejectedException(java.util.List.of("BLURRY")));

        assertThatThrownBy(() -> service.login("user@example.com", image))
                .isInstanceOf(FaceQualityRejectedException.class);

        verify(loginAttemptService, never()).recordFailure(anyString());
    }

    private static float[] vector(float value) {
        float[] vector = new float[128];
        java.util.Arrays.fill(vector, value);
        return vector;
    }
}
