package pnh.dev.qs.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import pnh.dev.qs.auth.dto.RefreshTokenData;
import pnh.dev.qs.auth.jwt.JwtProperties;
import pnh.dev.qs.exception.custom.UnauthorizedException;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JwtProperties jwtProperties;
    private DefaultRedisScript<String> consumeScript;

    @PostConstruct
    public void init() {
        consumeScript = new DefaultRedisScript<>();
        consumeScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/consume_refresh_token.lua")));
        consumeScript.setResultType(String.class);
    }

    public void saveRefreshToken(String tokenHash, Long userId, String deviceInfo, String ipAddress) {
        RefreshTokenData data = RefreshTokenData.builder()
                .userId(userId)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .status("active")
                .createdAt(Instant.now())
                .build();

        try {
            String json = objectMapper.writeValueAsString(data);
            String key = "refresh_token:" + tokenHash;
            redisTemplate.opsForValue().set(key, json, java.time.Duration.ofMillis(jwtProperties.refreshTokenExpiration()));
            redisTemplate.opsForSet().add("user_tokens:" + userId, tokenHash);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing refresh token data", e);
        }
    }

    public RefreshTokenData consumeRefreshToken(String tokenHash) {
        String key = "refresh_token:" + tokenHash;
        String result = redisTemplate.execute(consumeScript, Collections.singletonList(key));

        if (result == null) {
            return null; // Not found or expired
        }

        if (result.startsWith("REUSE_DETECTED:")) {
            Long userId = Long.parseLong(result.split(":")[1]);
            revokeAllUserTokens(userId);
            throw new UnauthorizedException("Token reuse detected. All sessions have been revoked for security.");
        }

        try {
            RefreshTokenData data = objectMapper.readValue(result, RefreshTokenData.class);
            redisTemplate.opsForSet().remove("user_tokens:" + data.getUserId(), tokenHash);
            return data;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing refresh token data", e);
        }
    }

    public void revokeAllUserTokens(Long userId) {
        String setKey = "user_tokens:" + userId;
        Set<String> tokenHashes = redisTemplate.opsForSet().members(setKey);
        
        if (tokenHashes != null && !tokenHashes.isEmpty()) {
            tokenHashes.forEach(hash -> redisTemplate.delete("refresh_token:" + hash));
        }
        redisTemplate.delete(setKey);
    }

    public void checkRateLimit(String endpoint, String ipAddress, int limit, long windowMinutes) {
        String key = "rate_limit:" + endpoint + ":" + ipAddress;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, java.time.Duration.ofMinutes(windowMinutes));
        }
        if (count != null && count > limit) {
            throw new pnh.dev.qs.exception.custom.BadRequestException("Too many requests. Please try again later.");
        }
    }
}
