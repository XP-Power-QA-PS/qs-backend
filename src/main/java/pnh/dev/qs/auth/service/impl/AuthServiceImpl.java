package pnh.dev.qs.auth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.auth.dto.response.AuthResponse;
import pnh.dev.qs.auth.dto.request.LoginRequest;
import pnh.dev.qs.auth.dto.RefreshTokenData;
import pnh.dev.qs.auth.jwt.JwtProperties;
import pnh.dev.qs.auth.jwt.JwtTokenProvider;
import pnh.dev.qs.auth.service.AuthService;
import pnh.dev.qs.auth.service.RefreshTokenService;
import pnh.dev.qs.exception.custom.UnauthorizedException;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.service.UserManagementService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserManagementService userManagementService;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        refreshTokenService.checkRateLimit("login", ipAddress, 5, 1);

        UserAccount user = userManagementService.verifyCredentials(request.getUsernameOrEmail(), request.getPassword());

        return generateAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String deviceInfo, String ipAddress) {
        RefreshTokenData tokenData = refreshTokenService.consumeRefreshToken(refreshToken);
        
        if (tokenData == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UserAccount user = userManagementService.getUserById(tokenData.getUserId());

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        return generateAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    public void logout(String accessToken) {
        if (accessToken != null && tokenProvider.validateToken(accessToken)) {
            String jti = tokenProvider.getJtiFromToken(accessToken);
            long remainingTime = tokenProvider.getRemainingExpiration(accessToken).toMillis();
            
            if (remainingTime > 0) {
                redisTemplate.opsForValue().set("blacklist:" + jti, "true", java.time.Duration.ofMillis(remainingTime));
            }
        }
    }

    @Override
    public void logoutAll(String accessToken) {
        if (accessToken != null && tokenProvider.validateToken(accessToken)) {
            Long userId = tokenProvider.getUserIdFromToken(accessToken);
            refreshTokenService.revokeAllUserTokens(userId);
            logout(accessToken); // Blacklist current access token
        }
    }

    private AuthResponse generateAuthResponse(UserAccount user, String deviceInfo, String ipAddress) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken();

        refreshTokenService.saveRefreshToken(refreshToken, user.getId(), deviceInfo, ipAddress);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProperties.accessTokenExpiration())
                .build();
    }
}
