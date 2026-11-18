package com.nextface.face;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.nextface.user.User;
import com.nextface.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class FaceEnrollmentServiceTest {

    @Mock
    private FaceProfileRepository faceProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FaceServiceClient faceServiceClient;

    private FaceEnrollmentService service;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User("user@example.com", "hash");
    private final MockMultipartFile image =
            new MockMultipartFile("image", "face.jpg", "image/jpeg", new byte[] {1, 2, 3});

    @BeforeEach
    void setUp() {
        service = new FaceEnrollmentService(faceProfileRepository, userRepository, faceServiceClient);
    }

    @Test
    void enrollCreatesProfileWithEmbedding() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(faceServiceClient.createEmbedding(image)).thenReturn(vector(0.25f));
        when(faceProfileRepository.saveAndFlush(any(FaceProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FaceProfileResponse response = service.enroll(userId, image, false);

        assertThat(response).isNotNull();
        ArgumentCaptor<FaceProfile> captor = ArgumentCaptor.forClass(FaceProfile.class);
        verify(faceProfileRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmbedding()).hasSize(128).contains(0.25f);
        assertThat(captor.getValue().getEmbeddingDimension()).isEqualTo(128);
    }

    @Test
    void enrollRejectsExistingProfileWithoutOverwrite() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId))
                .thenReturn(Optional.of(new FaceProfile(user, vector(0.1f))));

        assertThatThrownBy(() -> service.enroll(userId, image, false))
                .isInstanceOf(FaceProfileAlreadyExistsException.class);

        verifyNoInteractions(faceServiceClient);
        verify(faceProfileRepository, never()).saveAndFlush(any());
    }

    @Test
    void enrollWithOverwriteUpdatesEmbedding() {
        FaceProfile existing = new FaceProfile(user, vector(0.1f));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(faceServiceClient.createEmbedding(image)).thenReturn(vector(0.75f));
        when(faceProfileRepository.saveAndFlush(existing)).thenReturn(existing);

        service.enroll(userId, image, true);

        assertThat(existing.getEmbedding()[0]).isEqualTo(0.75f);
    }

    @Test
    void enrollPropagatesQualityRejection() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(faceServiceClient.createEmbedding(image))
                .thenThrow(new FaceQualityRejectedException(List.of("BLURRY")));

        assertThatThrownBy(() -> service.enroll(userId, image, false))
                .isInstanceOf(FaceQualityRejectedException.class);

        verify(faceProfileRepository, never()).saveAndFlush(any());
    }

    @Test
    void statusReportsMissingProfile() {
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThat(service.status(userId).enrolled()).isFalse();
    }

    @Test
    void deleteRejectsMissingProfile() {
        when(faceProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(userId))
                .isInstanceOf(FaceProfileNotFoundException.class);
    }

    private static float[] vector(float value) {
        float[] embedding = new float[128];
        java.util.Arrays.fill(embedding, value);
        return embedding;
    }
}
