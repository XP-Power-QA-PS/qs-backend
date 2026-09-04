package pnh.dev.qs.admin.service;

import pnh.dev.qs.admin.dto.request.RoleRequest;
import pnh.dev.qs.admin.dto.response.RoleResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminRoleService {
    RoleResponse createRole(RoleRequest request);
    RoleResponse updateRole(Long roleId, RoleRequest request);
    void deleteRole(Long roleId);
    Page<RoleResponse> getAllRoles(Pageable pageable);
}
