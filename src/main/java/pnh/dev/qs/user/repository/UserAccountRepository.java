package pnh.dev.qs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnh.dev.qs.user.entity.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
