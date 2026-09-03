package pnh.dev.qs.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.entity.PasswordResetToken;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.repository.PasswordResetTokenRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-minutes:30}")
    private long expirationMinutes;

    @Transactional
    public String generateResetToken(String email) {
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUserAccount(user);
        resetToken.setTokenHash(tokenHash);
        resetToken.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));

        passwordResetTokenRepository.save(resetToken);

        // TODO: Send email with rawToken
        // For now, return rawToken so we can test via API
        return rawToken;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Reset token has already been used");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        UserAccount user = resetToken.getUserAccount();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenService.revokeAllUserTokens(user.getId());
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
