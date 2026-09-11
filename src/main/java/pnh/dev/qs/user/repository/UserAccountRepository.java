package pnh.dev.qs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnh.dev.qs.user.entity.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>, JpaSpecificationExecutor<UserAccount> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM UserAccount u LEFT JOIN FETCH u.profile p LEFT JOIN FETCH u.roles r " +
           "WHERE u.isEnabled = true AND (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserAccount> searchActiveRecipients(@Param("keyword") String keyword);

    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<UserAccount> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE u.isEnabled = :status")
    Page<UserAccount> searchByStatus(@Param("status") Boolean status, Pageable pageable);

    @Query("SELECT u FROM UserAccount u WHERE u.isEnabled = :status AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<UserAccount> searchByStatusAndKeyword(@Param("status") Boolean status, @Param("keyword") String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM user_accounts WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC", 
           countQuery = "SELECT count(*) FROM user_accounts WHERE deleted_at IS NOT NULL", 
           nativeQuery = true)
    Page<UserAccount> findDeletedUsers(Pageable pageable);

    @Modifying
    @Query(value = "UPDATE user_accounts SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE user_accounts SET username = CONCAT('Deleted User ', id), email = CONCAT('deleted-', id, '@example.invalid'), is_enabled = false WHERE id = :id", nativeQuery = true)
    void anonymizeUser(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM user_profiles WHERE user_account_id = :id", nativeQuery = true)
    void deleteUserProfileByUserId(@Param("id") Long id);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_account_id = :id", nativeQuery = true)
    void deleteUserRolesByUserId(@Param("id") Long id);
}
