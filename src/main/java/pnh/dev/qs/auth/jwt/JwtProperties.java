package pnh.dev.qs.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    long accessTokenExpiration,
    long refreshTokenExpiration
) {}
