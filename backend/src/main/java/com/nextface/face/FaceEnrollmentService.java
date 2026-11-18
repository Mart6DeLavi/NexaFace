package com.nextface.face;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nextface.auth.InvalidCredentialsException;
import com.nextface.user.User;
import com.nextface.user.UserRepository;

/**
 * Enrollment of a single biometric profile per user.
 */
@Service
public class FaceEnrollmentService {

    private final FaceProfileRepository faceProfileRepository;
    private final UserRepository userRepository;
    private final FaceServiceClient faceServiceClient;

    public FaceEnrollmentService(
            FaceProfileRepository faceProfileRepository,
            UserRepository userRepository,
            FaceServiceClient faceServiceClient) {
        this.faceProfileRepository = faceProfileRepository;
        this.userRepository = userRepository;
        this.faceServiceClient = faceServiceClient;
    }

    @Transactional
    public FaceProfileResponse enroll(UUID userId, MultipartFile image, boolean overwrite) {
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidCredentialsException::new);

        FaceProfile existing = faceProfileRepository.findByUserId(userId).orElse(null);
        if (existing != null && !overwrite) {
            throw new FaceProfileAlreadyExistsException();
        }

        float[] embedding = faceServiceClient.createEmbedding(image);

        FaceProfile profile;
        if (existing == null) {
            profile = new FaceProfile(user, embedding);
        } else {
            existing.updateEmbedding(embedding);
            profile = existing;
        }
        return FaceProfileResponse.from(faceProfileRepository.saveAndFlush(profile));
    }

    @Transactional(readOnly = true)
    public FaceProfileStatusResponse status(UUID userId) {
        return faceProfileRepository.findByUserId(userId)
                .map(profile -> new FaceProfileStatusResponse(true, profile.getUpdatedAt()))
                .orElseGet(() -> new FaceProfileStatusResponse(false, null));
    }

    @Transactional
    public void delete(UUID userId) {
        FaceProfile profile = faceProfileRepository.findByUserId(userId)
                .orElseThrow(FaceProfileNotFoundException::new);
        faceProfileRepository.delete(profile);
    }
}
