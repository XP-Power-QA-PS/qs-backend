package pnh.dev.qs.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pnh.dev.qs.user.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
