package pnh.dev.qs.admin.service;

import pnh.dev.qs.admin.dto.request.AdminCreateUserRequest;
import pnh.dev.qs.admin.dto.request.AdminUpdateUserRequest;
import pnh.dev.qs.admin.dto.response.UserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminUserService {
    UserResponse createUser(AdminCreateUserRequest request);
    UserResponse updateUser(Long userId, AdminUpdateUserRequest request);
    void deleteUser(Long userId);
    Page<UserResponse> getAllUsers(Boolean status, String keyword, Pageable pageable);
    UserResponse getUserById(Long userId);

    void permanentlyDeleteUser(Long userId);
    void restoreUser(Long userId);
    Page<UserResponse> getDeletedUsers(Pageable pageable);
}
