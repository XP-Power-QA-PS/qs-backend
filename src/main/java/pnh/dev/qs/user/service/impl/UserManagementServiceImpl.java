package pnh.dev.qs.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.exception.custom.UnauthorizedException;
import pnh.dev.qs.user.dto.UserProfileDTO;
import pnh.dev.qs.user.dto.UserProfileUpdateRequest;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.entity.UserProfile;
import pnh.dev.qs.user.repository.RoleRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;
import pnh.dev.qs.user.repository.UserProfileRepository;
import pnh.dev.qs.user.service.UserManagementService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserAccount provisionUser(String username, String email, String rawPassword, Set<String> roleNames) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userAccountRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);

        String password = (rawPassword != null && !rawPassword.isBlank()) ? rawPassword : "123456";
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        if (roleNames != null && !roleNames.isEmpty()) {
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        } else {
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
            roles.add(userRole);
        }
        user.setRoles(roles);

        UserProfile profile = new UserProfile();
        profile.setUserAccount(user);
        user.setProfile(profile);

        return userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public UserAccount updateUser(Long userId, String email, String rawPassword, Boolean isEnabled, Set<String> roleNames) {
        UserAccount user = getUserById(userId);

        if (!user.getEmail().equals(email) && userAccountRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        user.setEmail(email);

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }

        if (isEnabled != null) {
            user.setEnabled(isEnabled);
        }

        if (roleNames != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        return userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public void softDeleteUser(Long userId) {
        UserAccount user = getUserById(userId);
        user.setDeletedAt(Instant.now());
        userAccountRepository.save(user);
    }

    @Override
    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        userAccountRepository.deleteUserProfileByUserId(userId);
        userAccountRepository.deleteUserRolesByUserId(userId);
        userAccountRepository.anonymizeUser(userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        userAccountRepository.restoreUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount getUserById(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccount getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAccount> searchUsers(Boolean status, String keyword, Pageable pageable) {
        if (status == null && (keyword == null || keyword.isBlank())) {
            return userAccountRepository.findAll(pageable);
        } else if (status == null) {
            return userAccountRepository.searchByKeyword(keyword, pageable);
        } else if (keyword == null || keyword.isBlank()) {
            return userAccountRepository.searchByStatus(status, pageable);
        } else {
            return userAccountRepository.searchByStatusAndKeyword(status, keyword, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserAccount> getDeletedUsers(Pageable pageable) {
        return userAccountRepository.findDeletedUsers(pageable);
    }

    @Override
    @Transactional
    public UserAccount verifyCredentials(String usernameOrEmail, String rawPassword) {
        UserAccount user = userAccountRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userAccountRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password")));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        user.setLastLoginAt(Instant.now());
        return userAccountRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO getCurrentUserProfile(Long userId) {
        UserAccount user = getUserById(userId);
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        UserAccount user = getUserById(userId);

        UserProfile profile = user.getProfile();
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserAccount(user);
            user.setProfile(profile);
        }

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) profile.setPhoneNumber(request.getPhoneNumber());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getBio() != null) profile.setBio(request.getBio());

        userProfileRepository.save(profile);
        return mapToDTO(user);
    }

    private UserProfileDTO mapToDTO(UserAccount user) {
        UserProfile profile = user.getProfile();
        return UserProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(profile != null ? profile.getFirstName() : null)
                .lastName(profile != null ? profile.getLastName() : null)
                .phoneNumber(profile != null ? profile.getPhoneNumber() : null)
                .avatarUrl(profile != null ? profile.getAvatarUrl() : null)
                .dateOfBirth(profile != null ? profile.getDateOfBirth() : null)
                .gender(profile != null ? profile.getGender() : null)
                .bio(profile != null ? profile.getBio() : null)
                .build();
    }
}
