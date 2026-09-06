package pnh.dev.qs.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pnh.dev.qs.user.dto.UserProfileDTO;
import pnh.dev.qs.user.dto.UserProfileUpdateRequest;
import pnh.dev.qs.user.entity.UserAccount;

import java.util.Set;

public interface UserManagementService {

    // Identity and Provisioning
    UserAccount provisionUser(String username, String email, String rawPassword, Set<String> roleNames);
    UserAccount updateUser(Long userId, String email, String rawPassword, Boolean isEnabled, Set<String> roleNames);
    
    // Lifecycle Management
    void softDeleteUser(Long userId);
    void permanentlyDeleteUser(Long userId);
    void restoreUser(Long userId);

    // Lookups
    UserAccount getUserById(Long userId);
    UserAccount getUserByUsername(String username);
    Page<UserAccount> searchUsers(Boolean status, String keyword, Pageable pageable);
    Page<UserAccount> getDeletedUsers(Pageable pageable);

    // Authentication Boundary
    UserAccount verifyCredentials(String usernameOrEmail, String rawPassword);

    // Profile Management
    UserProfileDTO getCurrentUserProfile(Long userId);
    UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request);
}
