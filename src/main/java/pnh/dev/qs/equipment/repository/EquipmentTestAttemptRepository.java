package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.EquipmentTestAttempt;

@Repository
public interface EquipmentTestAttemptRepository extends JpaRepository<EquipmentTestAttempt, Long> {
}

