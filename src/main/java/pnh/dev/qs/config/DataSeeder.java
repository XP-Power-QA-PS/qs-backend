package pnh.dev.qs.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminAccount();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            log.info("Seeding initial roles into the database...");
            
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            userRole.setDescription("Standard user role");
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("Administrator role");
            roleRepository.save(adminRole);
            
            log.info("Roles seeded successfully.");
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
