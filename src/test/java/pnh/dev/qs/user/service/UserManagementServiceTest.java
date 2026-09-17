package pnh.dev.qs.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.security.CustomUserDetailsService;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.repository.RoleRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;
import pnh.dev.qs.user.repository.UserProfileRepository;
import pnh.dev.qs.user.service.impl.UserManagementServiceImpl;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    private UserManagementServiceImpl userManagementService;

    @BeforeEach
    void setUp() {
        userManagementService = new UserManagementServiceImpl(
                userAccountRepository,
                userProfileRepository,
                roleRepository,
                passwordEncoder,
                customUserDetailsService
        );
    }

    @Test
    @DisplayName("Operator without email should be provisioned successfully")
    void provisionUser_operatorWithoutEmail_success() {
        // Arrange
        String username = "OP-10023";
        String email = null;
        Set<String> roles = Set.of("ROLE_OPERATOR");

        when(userAccountRepository.existsByUsername(username)).thenReturn(false);
        Role operatorRole = new Role();
        operatorRole.setName("ROLE_OPERATOR");
        when(roleRepository.findByName("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_default_pwd");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserAccount result = userManagementService.provisionUser(username, email, null, roles);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertNull(result.getEmail());
        verify(userAccountRepository, never()).existsByEmail(any());
        verify(userAccountRepository).save(any(UserAccount.class));
    }

    @Test
    @DisplayName("Operator with blank email should sanitize to null and succeed")
    void provisionUser_operatorWithBlankEmail_sanitizesToNull() {
        // Arrange
        String username = "OP-10024";
        String email = "   ";
        Set<String> roles = Set.of("ROLE_OPERATOR");

        when(userAccountRepository.existsByUsername(username)).thenReturn(false);
        Role operatorRole = new Role();
        operatorRole.setName("ROLE_OPERATOR");
        when(roleRepository.findByName("ROLE_OPERATOR")).thenReturn(Optional.of(operatorRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_default_pwd");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserAccount result = userManagementService.provisionUser(username, email, "pwd123", roles);

        // Assert
        assertNotNull(result);
        assertNull(result.getEmail());
        verify(userAccountRepository, never()).existsByEmail(any());
    }

    @Test
    @DisplayName("Inspector without email should throw BadRequestException")
    void provisionUser_inspectorWithoutEmail_throwsBadRequest() {
        // Arrange
        String username = "INSP-01";
        String email = "";
        Set<String> roles = Set.of("ROLE_INSPECTOR");

        when(userAccountRepository.existsByUsername(username)).thenReturn(false);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                userManagementService.provisionUser(username, email, null, roles));

        assertTrue(ex.getMessage().contains("Email is required"));
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Admin without email should throw BadRequestException")
    void provisionUser_adminWithoutEmail_throwsBadRequest() {
        // Arrange
        String username = "admin.user";
        Set<String> roles = Set.of("ROLE_ADMIN");

        when(userAccountRepository.existsByUsername(username)).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                userManagementService.provisionUser(username, null, null, roles));
    }

    @Test
    @DisplayName("Inspector with valid email should succeed")
    void provisionUser_inspectorWithValidEmail_success() {
        // Arrange
        String username = "INSP-02";
        String email = "inspector@xppower.com";
        Set<String> roles = Set.of("ROLE_INSPECTOR");

        when(userAccountRepository.existsByUsername(username)).thenReturn(false);
        when(userAccountRepository.existsByEmail(email)).thenReturn(false);
        Role inspectorRole = new Role();
        inspectorRole.setName("ROLE_INSPECTOR");
        when(roleRepository.findByName("ROLE_INSPECTOR")).thenReturn(Optional.of(inspectorRole));
        when(passwordEncoder.encode(any())).thenReturn("hashed_pwd");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserAccount result = userManagementService.provisionUser(username, email, "strongPwd", roles);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    @DisplayName("Updating role from Operator to Inspector without providing email should throw BadRequestException")
    void updateUser_promoteToInspectorWithoutEmail_throwsBadRequest() {
        // Arrange
        Long userId = 100L;
        UserAccount existingUser = new UserAccount();
        existingUser.setId(userId);
        existingUser.setUsername("OP-PROMOTE");
        existingUser.setEmail(null);
        Role operatorRole = new Role();
        operatorRole.setName("ROLE_OPERATOR");
        existingUser.setRoles(Set.of(operatorRole));

        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        // Act & Assert (try to update roles to ROLE_INSPECTOR with empty email)
        assertThrows(BadRequestException.class, () ->
                userManagementService.updateUser(userId, "", null, true, Set.of("ROLE_INSPECTOR")));
    }
}
