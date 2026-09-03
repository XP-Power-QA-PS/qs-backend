package pnh.dev.qs.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pnh.dev.qs.auth.dto.response.AuthResponse;
import pnh.dev.qs.auth.dto.request.ForgotPasswordRequest;
import pnh.dev.qs.auth.dto.request.LoginRequest;
import pnh.dev.qs.auth.dto.request.RefreshTokenRequest;
import pnh.dev.qs.auth.dto.request.RegisterRequest;
import pnh.dev.qs.auth.dto.request.ResetPasswordRequest;
import pnh.dev.qs.auth.service.AuthService;
import pnh.dev.qs.auth.service.PasswordResetService;
import pnh.dev.qs.auth.service.RefreshTokenService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
                                                 HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return new ResponseEntity<>(authService.register(request, userAgent, ipAddress), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
                                              HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return ResponseEntity.ok(authService.login(request, userAgent, ipAddress));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String userAgent,
                                                HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken(), userAgent, ipAddress));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authService.logoutAll(token.substring(7));
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request,
                                                              HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        refreshTokenService.checkRateLimit("forgot-password", ipAddress, 3, 15);
        
        String resetToken = passwordResetService.generateResetToken(request.getEmail());
        
        // Return token directly for API testing (in production this would be sent via email)
        return ResponseEntity.ok(Map.of("message", "Password reset token generated", "token", resetToken));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }

    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || remoteAddr.isEmpty()) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}
