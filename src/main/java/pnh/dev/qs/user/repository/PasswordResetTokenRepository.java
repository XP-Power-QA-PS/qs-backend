package pnh.dev.qs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnh.dev.qs.user.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
