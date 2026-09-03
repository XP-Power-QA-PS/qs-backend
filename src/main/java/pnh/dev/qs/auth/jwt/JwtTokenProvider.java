package pnh.dev.qs.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserAccount user) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + jwtProperties.accessTokenExpiration());

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("roles", roles)
                .id(UUID.randomUUID().toString()) // jti
                .issuedAt(new Date(now))
                .expiration(validity)
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        String subject = getClaims(token).getSubject();
        return Long.parseLong(subject);
    }

    public String getJtiFromToken(String token) {
        return getClaims(token).getId();
    }

    public Duration getRemainingExpiration(String token) {
        Date expiration = getClaims(token).getExpiration();
        long diff = expiration.getTime() - System.currentTimeMillis();
        return diff > 0 ? Duration.ofMillis(diff) : Duration.ZERO;
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
