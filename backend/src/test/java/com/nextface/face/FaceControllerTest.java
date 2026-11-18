package com.nextface.face;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;

import com.nextface.auth.AuthResponse;
import com.nextface.auth.AuthenticatedUser;
import com.nextface.auth.JwtService;
import com.nextface.user.UserResponse;

@WebMvcTest(controllers = FaceController.class,
        properties = "nextface.cors.allowed-origins=http://localhost:3000")
@AutoConfigureMockMvc(addFilters = false)
class FaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FaceEnrollmentService enrollmentService;

    @MockitoBean
    private FaceAuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    private final AuthenticatedUser user =
            new AuthenticatedUser(UUID.randomUUID(), "user@example.com");

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder builder) {
        RequestPostProcessor authenticated = request -> {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
            return request;
        };
        return builder.with(authenticated);
    }

    @Test
    void enrollReturnsProfileTimestamps() throws Exception {
        Instant enrolledAt = Instant.parse("2026-11-18T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-11-18T10:00:00Z");
        when(enrollmentService.enroll(eq(user.id()), any(MultipartFile.class), eq(false)))
                .thenReturn(new FaceProfileResponse(UUID.randomUUID(), enrolledAt, updatedAt));

        MockMultipartFile image = new MockMultipartFile(
                "image", "face.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(asUser(multipart("/api/face/enroll").file(image)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledAt").value("2026-11-18T10:00:00Z"))
                .andExpect(jsonPath("$.updatedAt").value("2026-11-18T10:00:00Z"));
    }

    @Test
    void enrollRejectsMissingImagePart() throws Exception {
        mockMvc.perform(asUser(multipart("/api/face/enroll")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FACE_IMAGE_REQUIRED"));
    }

    @Test
    void enrollReturnsConflictForExistingProfile() throws Exception {
        when(enrollmentService.enroll(eq(user.id()), any(MultipartFile.class), eq(false)))
                .thenThrow(new FaceProfileAlreadyExistsException());

        MockMultipartFile image = new MockMultipartFile(
                "image", "face.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(asUser(multipart("/api/face/enroll").file(image)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("FACE_PROFILE_EXISTS"));
    }

    @Test
    void profileReportsEnrollmentStatus() throws Exception {
        when(enrollmentService.status(user.id())).thenReturn(
                new FaceProfileStatusResponse(true, Instant.parse("2026-11-18T10:00:00Z")));

        mockMvc.perform(asUser(get("/api/face/profile")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolled").value(true));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mockMvc.perform(asUser(delete("/api/face/profile")))
                .andExpect(status().isNoContent());

        verify(enrollmentService).delete(user.id());
    }

    @Test
    void loginReturnsJwt() throws Exception {
        when(authenticationService.login(eq(user.email()), any(MultipartFile.class)))
                .thenReturn(AuthResponse.bearer(
                        "token", 3600, new UserResponse(user.id(), user.email(), Instant.now())));

        MockMultipartFile image = new MockMultipartFile(
                "image", "face.jpg", "image/jpeg", new byte[] {1, 2, 3});

        mockMvc.perform(multipart("/api/face/login")
                        .file(image)
                        .param("email", user.email()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(user.email()));
    }

    @Test
    void loginRejectsMissingImagePart() throws Exception {
        mockMvc.perform(multipart("/api/face/login").param("email", user.email()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("FACE_IMAGE_REQUIRED"));
    }
}
