package pnh.dev.qs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.user.entity.Role;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.repository.RoleRepository;
import pnh.dev.qs.user.repository.UserAccountRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin11}")
    private String adminPassword;

    @Value("${app.admin.email:admin@healthsense.com}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        seedRoles();
        seedAdminAccount();
    }

    private void seedRoles() {
        seedRoleIfNotExists("ROLE_USER", "Standard user role");
        seedRoleIfNotExists("ROLE_ADMIN", "Administrator role");
        seedRoleIfNotExists("ROLE_OPERATOR", "Operator / Line worker role - no email required");
        seedRoleIfNotExists("ROLE_INSPECTOR", "QC Inspector / KCS role - email required");
        seedRoleIfNotExists("ROLE_QC_ENGINEER", "Quality Engineer role - email required");
        seedRoleIfNotExists("ROLE_SUPERVISOR", "Production / Line Supervisor role - email required");
    }

    private void seedRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            role.setDescription(description);
            roleRepository.save(role);
            log.info("Role {} seeded successfully.", name);
        }
    }

    private void seedAdminAccount() {
        if (!userAccountRepository.existsByUsername(adminUsername)) {
            log.info("Seeding admin account...");
            
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

            UserAccount admin = new UserAccount();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setEnabled(true);
            admin.getRoles().add(adminRole);
            // createdBy can be set to 'system' since there's no auditor context yet
            admin.setCreatedBy("system");

            userAccountRepository.save(admin);
            log.info("Admin account seeded successfully.");
        }
    }
}
