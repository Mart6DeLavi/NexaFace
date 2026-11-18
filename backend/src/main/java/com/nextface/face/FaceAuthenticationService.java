package com.nextface.face;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nextface.audit.AuditEventType;
import com.nextface.audit.AuditService;
import com.nextface.auth.AuthResponse;
import com.nextface.auth.JwtService;
import com.nextface.user.User;
import com.nextface.user.UserRepository;
import com.nextface.user.UserResponse;

/**
 * 1:1 face verification: the email identifies the account, the captured image is
 * compared with the stored embedding and a JWT is issued only on a match.
 */
@Service
public class FaceAuthenticationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final FaceProfileRepository faceProfileRepository;
    private final FaceServiceClient faceServiceClient;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final AuditService auditService;
    private final double matchThreshold;

    public FaceAuthenticationService(
            UserRepository userRepository,
            FaceProfileRepository faceProfileRepository,
            FaceServiceClient faceServiceClient,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            AuditService auditService,
            @Value("${nextface.face.match-threshold}") double matchThreshold) {
        this.userRepository = userRepository;
        this.faceProfileRepository = faceProfileRepository;
        this.faceServiceClient = faceServiceClient;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
        this.matchThreshold = matchThreshold;
    }

    @Transactional
    public AuthResponse login(String email, MultipartFile image) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        loginAttemptService.assertAllowed(normalizedEmail);

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new FaceAuthenticationFailedException();
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (user == null || !user.isEnabled()) {
            registerFailure(normalizedEmail, null);
            throw new FaceAuthenticationFailedException();
        }

        FaceProfile profile = faceProfileRepository.findByUserId(user.getId())
                .orElseThrow(FaceProfileNotFoundException::new);

        float[] candidate = faceServiceClient.createEmbedding(image);
        double similarity = EmbeddingMatcher.cosineSimilarity(profile.getEmbedding(), candidate);
        if (similarity < matchThreshold) {
            registerFailure(normalizedEmail, user.getId());
            throw new FaceAuthenticationFailedException();
        }

        loginAttemptService.reset(normalizedEmail);
        auditService.record(user.getId(), AuditEventType.FACE_LOGIN, true);

        return AuthResponse.bearer(
                jwtService.generateToken(user.getId(), user.getEmail()),
                jwtService.expirationSeconds(),
                UserResponse.from(user));
    }

    private void registerFailure(String email, java.util.UUID userId) {
        loginAttemptService.recordFailure(email);
        auditService.record(userId, AuditEventType.FACE_LOGIN, false);
    }
}
