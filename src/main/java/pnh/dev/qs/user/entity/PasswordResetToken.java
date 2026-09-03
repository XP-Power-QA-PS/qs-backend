package pnh.dev.qs.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pnh.dev.qs.common.entity.BaseEntity;

import java.time.Instant;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount userAccount;

    @Column(name = "token_hash", unique = true, nullable = false)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;
}
