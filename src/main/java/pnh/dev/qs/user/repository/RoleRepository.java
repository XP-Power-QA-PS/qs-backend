package pnh.dev.qs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnh.dev.qs.user.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
