package pnh.dev.qs.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pnh.dev.qs.admin.dto.request.UpdatePermissionMatrixRequest;
import pnh.dev.qs.admin.dto.response.PermissionMatrixResponse;
import pnh.dev.qs.admin.service.impl.AdminPermissionServiceImpl;
import pnh.dev.qs.user.entity.Permission;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.repository.PermissionRepository;
import pnh.dev.qs.user.repository.RoleRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPermissionServiceTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    private AdminPermissionServiceImpl adminPermissionService;

    @BeforeEach
    void setUp() {
        adminPermissionService = new AdminPermissionServiceImpl(permissionRepository, roleRepository);
    }

    @Test
    void getPermissionMatrix_shouldReturnGroupedModulesAndRoles() {
        // Arrange
        Permission p1 = new Permission();
        p1.setId(101L);
        p1.setCode("GONOGO_VIEW");
        p1.setModule("GONOGO");
        p1.setAction("VIEW");
        p1.setName("View Test Logs");

        Permission p2 = new Permission();
        p2.setId(102L);
        p2.setCode("COMPLAINT_VIEW");
        p2.setModule("COMPLAINT");
        p2.setAction("VIEW");
        p2.setName("View Complaints");

        when(permissionRepository.findAllByOrderByModuleAscActionAsc()).thenReturn(List.of(p1, p2));

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(new HashSet<>(List.of(p1, p2)));

        Role opRole = new Role();
        opRole.setId(2L);
        opRole.setName("ROLE_OPERATOR");
        opRole.setPermissions(new HashSet<>(List.of(p1)));

        when(roleRepository.findAll()).thenReturn(new ArrayList<>(List.of(adminRole, opRole)));

        // Act
        PermissionMatrixResponse response = adminPermissionService.getPermissionMatrix();

        // Assert
        assertNotNull(response);
        assertEquals(5, response.getModules().size()); // 5 standard modules
        assertEquals(2, response.getRoles().size());
        assertTrue(response.getRolePermissions().get("1").contains("101"));
        assertTrue(response.getRolePermissions().get("1").contains("102"));
        assertTrue(response.getRolePermissions().get("2").contains("101"));
        assertFalse(response.getRolePermissions().get("2").contains("102"));
    }

    @Test
    void updatePermissionMatrix_shouldProtectAdminAndApplyToOtherRoles() {
        // Arrange
        Permission p1 = new Permission();
        p1.setId(101L);
        p1.setCode("GONOGO_VIEW");
        p1.setModule("GONOGO");

        Permission p2 = new Permission();
        p2.setId(102L);
        p2.setCode("COMPLAINT_VIEW");
        p2.setModule("COMPLAINT");

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));
        when(permissionRepository.findAllByOrderByModuleAscActionAsc()).thenReturn(List.of(p1, p2));

        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ROLE_ADMIN");
        adminRole.setPermissions(new HashSet<>(List.of(p1, p2)));

        Role opRole = new Role();
        opRole.setId(2L);
        opRole.setName("ROLE_OPERATOR");
        opRole.setPermissions(new HashSet<>());

        when(roleRepository.findById(1L)).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(opRole));
        when(roleRepository.findAll()).thenReturn(new ArrayList<>(List.of(adminRole, opRole)));

        // Attempting to strip admin permissions to empty, but give p2 to operator
        Map<Long, List<Long>> updates = new HashMap<>();
        updates.put(1L, List.of()); // Attempt to revoke admin
        updates.put(2L, List.of(102L)); // Grant complaint view to operator

        UpdatePermissionMatrixRequest request = new UpdatePermissionMatrixRequest(updates);

        // Act
        PermissionMatrixResponse response = adminPermissionService.updatePermissionMatrix(request);

        // Assert
        assertNotNull(response);
        // Admin must still retain all permissions
        assertEquals(2, adminRole.getPermissions().size());
        // Operator should have received p2
        assertEquals(1, opRole.getPermissions().size());
        assertTrue(opRole.getPermissions().contains(p2));
        verify(roleRepository, times(2)).save(any(Role.class));
    }
}
