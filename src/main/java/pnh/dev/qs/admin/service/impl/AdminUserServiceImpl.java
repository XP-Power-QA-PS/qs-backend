package pnh.dev.qs.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.admin.dto.request.AdminCreateUserRequest;
import pnh.dev.qs.admin.dto.request.AdminUpdateUserRequest;
import pnh.dev.qs.admin.dto.response.UserResponse;
import pnh.dev.qs.admin.service.AdminUserService;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.service.UserManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserManagementService userManagementService;

    @Override
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest request) {
        UserAccount user = userManagementService.provisionUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRoles()
        );
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        UserAccount user = userManagementService.updateUser(
                userId,
                request.getEmail(),
                request.getPassword(),
                request.getIsEnabled(),
                request.getRoles()
        );
        return mapToResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        userManagementService.softDeleteUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Boolean status, String keyword, Pageable pageable) {
        return userManagementService.searchUsers(status, keyword, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return mapToResponse(userManagementService.getUserById(userId));
    }

    @Override
    @Transactional
    public void permanentlyDeleteUser(Long userId) {
        userManagementService.permanentlyDeleteUser(userId);
    }

    @Override
    @Transactional
    public void restoreUser(Long userId) {
        userManagementService.restoreUser(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getDeletedUsers(Pageable pageable) {
        return userManagementService.getDeletedUsers(pageable).map(this::mapToResponse);
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
