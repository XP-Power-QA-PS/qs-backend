package pnh.dev.qs.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.admin.dto.request.RoleRequest;
import pnh.dev.qs.admin.dto.response.RoleResponse;
import pnh.dev.qs.admin.service.AdminRoleService;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleServiceImpl implements AdminRoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        String roleName = request.getName().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        if (roleRepository.findByName(roleName).isPresent()) {
            throw new DuplicateResourceException("Role already exists");
        }

        Role role = new Role();
        role.setName(roleName);
        role.setDescription(request.getDescription());
        
        Role savedRole = roleRepository.save(role);
        return mapToResponse(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long roleId, RoleRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        
        // Disallow editing standard roles just in case
        if (role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_USER")) {
            throw new BadRequestException("Cannot modify system roles");
        }

        String newRoleName = request.getName().toUpperCase();
        if (!newRoleName.startsWith("ROLE_")) {
            newRoleName = "ROLE_" + newRoleName;
        }

        if (!role.getName().equals(newRoleName) && roleRepository.findByName(newRoleName).isPresent()) {
            throw new DuplicateResourceException("Role name already exists");
        }

        role.setName(newRoleName);
        role.setDescription(request.getDescription());
        
        Role updatedRole = roleRepository.save(role);
        return mapToResponse(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        if (role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_USER")) {
            throw new BadRequestException("Cannot delete system roles");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
