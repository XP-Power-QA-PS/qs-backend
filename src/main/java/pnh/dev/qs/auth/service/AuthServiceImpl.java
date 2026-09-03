package pnh.dev.qs.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.auth.dto.response.AuthResponse;
import pnh.dev.qs.auth.dto.request.LoginRequest;
import pnh.dev.qs.auth.dto.RefreshTokenData;
import pnh.dev.qs.auth.dto.request.RegisterRequest;
import pnh.dev.qs.auth.jwt.JwtProperties;
import pnh.dev.qs.auth.jwt.JwtTokenProvider;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.UnauthorizedException;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.entity.UserProfile;
import pnh.dev.qs.user.repository.RoleRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;

import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, String deviceInfo, String ipAddress) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not set in database"));
        user.setRoles(Collections.singleton(userRole));

        UserProfile profile = new UserProfile();
        profile.setUserAccount(user);
        user.setProfile(profile);

        user = userAccountRepository.save(user);

        return generateAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo, String ipAddress) {
        refreshTokenService.checkRateLimit("login", ipAddress, 5, 1);

        UserAccount user = userAccountRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> userAccountRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password")));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        user.setLastLoginAt(Instant.now());
        userAccountRepository.save(user);

        return generateAuthResponse(user, deviceInfo, ipAddress);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, String deviceInfo, String ipAddress) {
        RefreshTokenData tokenData = refreshTokenService.consumeRefreshToken(refreshToken);
        
        if (tokenData == null) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        UserAccount user = userAccountRepository.findById(tokenData.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

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
