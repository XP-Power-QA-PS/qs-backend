package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.Floor;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {
}
