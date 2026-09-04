package pnh.dev.qs.auth.service;

import pnh.dev.qs.auth.dto.response.AuthResponse;
import pnh.dev.qs.auth.dto.request.LoginRequest;
public interface AuthService {
    AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress);
    AuthResponse refreshToken(String refreshToken, String deviceInfo, String ipAddress);
    void logout(String accessToken);
    void logoutAll(String accessToken);
}
