package com.nextface.face;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nextface.auth.AuthenticatedUser;

/**
 * Face enrollment endpoints. All of them require a valid JWT.
 */
@RestController
@RequestMapping("/api/face")
public class FaceController {

    private final FaceEnrollmentService enrollmentService;

    public FaceController(FaceEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping(value = "/enroll", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FaceProfileResponse enroll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestPart("image") MultipartFile image,
            @RequestParam(name = "overwrite", defaultValue = "false") boolean overwrite) {
        return enrollmentService.enroll(user.id(), image, overwrite);
    }

    @GetMapping("/profile")
    public FaceProfileStatusResponse profile(@AuthenticationPrincipal AuthenticatedUser user) {
        return enrollmentService.status(user.id());
    }

    @DeleteMapping("/profile")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user) {
        enrollmentService.delete(user.id());
        return ResponseEntity.noContent().build();
    }
}
