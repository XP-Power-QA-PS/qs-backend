package pnh.dev.qs.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.admin.dto.request.AdminCreateUserRequest;
import pnh.dev.qs.admin.dto.request.AdminUpdateUserRequest;
import pnh.dev.qs.admin.dto.response.UserResponse;
import pnh.dev.qs.admin.service.AdminUserService;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.entity.UserProfile;
import pnh.dev.qs.user.repository.RoleRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        if (userAccountRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already exists");
        }
        if (userAccountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // If password is provided use it, else generate default "123456"
        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank()) 
                             ? request.getPassword() : "123456";
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (String roleName : request.getRoles()) {
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

        UserAccount savedUser = userAccountRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userAccountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        user.setEmail(request.getEmail());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIsEnabled() != null) {
            user.setEnabled(request.getIsEnabled());
        }

        if (request.getRoles() != null) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        UserAccount updatedUser = userAccountRepository.save(user);
        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // Using soft delete by setting deletedAt
        user.setDeletedAt(Instant.now());
        userAccountRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Boolean status, String keyword, Pageable pageable) {
        if (status == null && (keyword == null || keyword.isBlank())) {
            return userAccountRepository.findAll(pageable).map(this::mapToResponse);
        } else if (status == null) {
            return userAccountRepository.searchByKeyword(keyword, pageable).map(this::mapToResponse);
        } else if (keyword == null || keyword.isBlank()) {
            return userAccountRepository.searchByStatus(status, pageable).map(this::mapToResponse);
        } else {
            return userAccountRepository.searchByStatusAndKeyword(status, keyword, pageable).map(this::mapToResponse);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        // We do not check if it exists in the normal way because findById ignores deleted users
        // If the user doesn't exist at all, the native queries will just update 0 rows, which is fine
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
    public Page<UserResponse> getDeletedUsers(Pageable pageable) {
        return userAccountRepository.findDeletedUsers(pageable).map(this::mapToResponse);
    }

    private UserResponse mapToResponse(UserAccount user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .isEnabled(user.isEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .firstName(user.getProfile() != null ? user.getProfile().getFirstName() : null)
                .lastName(user.getProfile() != null ? user.getProfile().getLastName() : null)
                .phoneNumber(user.getProfile() != null ? user.getProfile().getPhoneNumber() : null)
                .avatarUrl(user.getProfile() != null ? user.getProfile().getAvatarUrl() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
