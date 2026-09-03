package pnh.dev.qs.auth.service;

import pnh.dev.qs.auth.dto.response.AuthResponse;
import pnh.dev.qs.auth.dto.request.LoginRequest;
import pnh.dev.qs.auth.dto.request.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String deviceInfo, String ipAddress);
    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress);
    AuthResponse refreshToken(String refreshToken, String deviceInfo, String ipAddress);
    void logout(String accessToken);
    void logoutAll(String accessToken);
}
