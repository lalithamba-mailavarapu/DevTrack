
package in.strikes.devTrack.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.strikes.devTrack.dto.ChangePasswordRequest;
import in.strikes.devTrack.dto.CurrentUserResponse;
import in.strikes.devTrack.dto.ForgotPasswordRequest;
import in.strikes.devTrack.dto.LoginRequest;
import in.strikes.devTrack.dto.LoginResponse;
import in.strikes.devTrack.dto.RegisterRequest;
import in.strikes.devTrack.dto.RegisterResponse;
import in.strikes.devTrack.dto.ResetPasswordRequest;
import in.strikes.devTrack.dto.UpdateProfileRequest;
import in.strikes.devTrack.dto.VerifyEmailRequest;
import in.strikes.devTrack.service.AuthService;
import in.strikes.devTrack.service.EmailVerificationService;
import in.strikes.devTrack.service.PasswordResetService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(
        AuthService authService,
        PasswordResetService passwordResetService,
        EmailVerificationService emailVerificationService) {

    this.authService = authService;
    this.passwordResetService = passwordResetService;
    this.emailVerificationService = emailVerificationService;
}


       @PostMapping("/verify-email")
public ResponseEntity<?> verifyEmail(
        @RequestBody VerifyEmailRequest request) {

    emailVerificationService.verifyEmail(
            request.getEmail(),
            request.getOtp()
    );

    return ResponseEntity.ok(
            Map.of("message", "Email verified and account created successfully")
    );
}

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(
            org.springframework.security.core.Authentication authentication) {

        CurrentUserResponse response =
                authService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(response);
    }


    @PutMapping("/me")
    public ResponseEntity<CurrentUserResponse> updateMe(
            org.springframework.security.core.Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        CurrentUserResponse response =
                authService.updateCurrentUser(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            org.springframework.security.core.Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        authService.changePassword(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(request.getEmail());

        return ResponseEntity.ok(
                "If an account exists for that email, a password reset link will be sent."
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
                request.getToken(),
                request.getNewPassword()
        );

        return ResponseEntity.ok("Password reset successfully");
    }

}
