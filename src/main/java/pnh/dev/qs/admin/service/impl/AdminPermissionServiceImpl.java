package pnh.dev.qs.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.admin.dto.request.UpdatePermissionMatrixRequest;
import pnh.dev.qs.admin.dto.response.PermissionItemResponse;
import pnh.dev.qs.admin.dto.response.PermissionMatrixModuleGroup;
import pnh.dev.qs.admin.dto.response.PermissionMatrixResponse;
import pnh.dev.qs.admin.dto.response.RoleResponse;
import pnh.dev.qs.admin.service.AdminPermissionService;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.entity.Permission;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.repository.PermissionRepository;
import pnh.dev.qs.user.repository.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminPermissionServiceImpl implements AdminPermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    private static final List<String> ORDERED_MODULES = List.of(
            "GONOGO", "EQUIPMENT", "COMPLAINT", "COMMUNICATION", "SYSTEM_ADMIN"
    );

    private static final Map<String, String[]> MODULE_METADATA = Map.of(
            "GONOGO", new String[]{"Go/No-Go & Daily Testing", "Daily shift equipment inspection and Go/No-Go testing"},
            "EQUIPMENT", new String[]{"Equipments & Floors", "Manage equipment catalog, test programs, and plant floor layout"},
            "COMPLAINT", new String[]{"Customer Complaints (CAPA)", "Handle customer quality complaints and 8D CAPA workflows"},
            "COMMUNICATION", new String[]{"CFT Meetings & Email Alerts", "Cross-functional team meetings and automated notification alerts"},
            "SYSTEM_ADMIN", new String[]{"System & Security Administration", "Manage user accounts, system roles, and access control matrix"}
    );

    private static final List<String> ROLE_PRIORITY_ORDER = List.of(
            "ROLE_ADMIN", "ROLE_SUPERVISOR", "ROLE_QC_ENGINEER", "ROLE_INSPECTOR", "ROLE_OPERATOR", "ROLE_USER"
    );

    @Override
    @Transactional(readOnly = true)
    public PermissionMatrixResponse getPermissionMatrix() {
        List<Permission> allPermissions = permissionRepository.findAllByOrderByModuleAscActionAsc();
        List<Role> allRoles = roleRepository.findAll();

        // Sort roles by predefined hierarchy, then alphabetical
        allRoles.sort((r1, r2) -> {
            int idx1 = ROLE_PRIORITY_ORDER.indexOf(r1.getName());
            int idx2 = ROLE_PRIORITY_ORDER.indexOf(r2.getName());
            if (idx1 != -1 && idx2 != -1) return Integer.compare(idx1, idx2);
            if (idx1 != -1) return -1;
            if (idx2 != -1) return 1;
            return r1.getName().compareTo(r2.getName());
        });

        // Group permissions by module
        Map<String, List<Permission>> permsByModule = allPermissions.stream()
                .collect(Collectors.groupingBy(Permission::getModule));

        List<PermissionMatrixModuleGroup> moduleGroups = new ArrayList<>();
        for (String modKey : ORDERED_MODULES) {
            List<Permission> modulePerms = permsByModule.getOrDefault(modKey, Collections.emptyList());
            String[] meta = MODULE_METADATA.getOrDefault(modKey, new String[]{modKey, ""});

            List<PermissionItemResponse> permItems = modulePerms.stream()
                    .map(p -> PermissionItemResponse.builder()
                            .id(p.getId())
                            .code(p.getCode())
                            .module(p.getModule())
                            .action(p.getAction())
                            .name(p.getName())
                            .description(p.getDescription())
                            .build())
                    .toList();

            moduleGroups.add(PermissionMatrixModuleGroup.builder()
                    .moduleKey(modKey)
                    .moduleName(meta[0])
                    .description(meta[1])
                    .permissions(permItems)
                    .build());
        }

        // Add any additional modules not in ORDERED_MODULES
        for (Map.Entry<String, List<Permission>> entry : permsByModule.entrySet()) {
            if (!ORDERED_MODULES.contains(entry.getKey())) {
                List<PermissionItemResponse> permItems = entry.getValue().stream()
                        .map(p -> PermissionItemResponse.builder()
                                .id(p.getId())
                                .code(p.getCode())
                                .module(p.getModule())
                                .action(p.getAction())
                                .name(p.getName())
                                .description(p.getDescription())
                                .build())
                        .toList();

                moduleGroups.add(PermissionMatrixModuleGroup.builder()
                        .moduleKey(entry.getKey())
                        .moduleName(entry.getKey())
                        .description("")
                        .permissions(permItems)
                        .build());
            }
        }

        // Map roles to response
        List<RoleResponse> roleResponses = allRoles.stream()
                .map(r -> RoleResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build())
                .toList();

        // Build roleId -> set of permissionIds map
        Map<String, Set<String>> rolePermissionsMap = new HashMap<>();
        for (Role role : allRoles) {
            Set<String> permIds = role.getPermissions().stream()
                    .map(p -> String.valueOf(p.getId()))
                    .collect(Collectors.toSet());
            rolePermissionsMap.put(String.valueOf(role.getId()), permIds);
        }

        return PermissionMatrixResponse.builder()
                .modules(moduleGroups)
                .roles(roleResponses)
                .rolePermissions(rolePermissionsMap)
                .build();
    }

    @Override
    @Transactional
    public PermissionMatrixResponse updatePermissionMatrix(UpdatePermissionMatrixRequest request) {
        if (request.getRolePermissions() == null || request.getRolePermissions().isEmpty()) {
            return getPermissionMatrix();
        }

        List<Permission> allPermissions = permissionRepository.findAll();
        Map<Long, Permission> permById = allPermissions.stream()
                .collect(Collectors.toMap(Permission::getId, p -> p));

        for (Map.Entry<Long, List<Long>> entry : request.getRolePermissions().entrySet()) {
            Long roleId = entry.getKey();
            List<Long> requestedPermIds = entry.getValue();

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

            // Hardened safety: Super Admin must retain all permissions
            if ("ROLE_ADMIN".equals(role.getName())) {
                role.setPermissions(new HashSet<>(allPermissions));
            } else {
                Set<Permission> targetPerms = requestedPermIds.stream()
                        .map(permById::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                role.setPermissions(targetPerms);
            }

            roleRepository.save(role);
        }

        return getPermissionMatrix();
    }
}
