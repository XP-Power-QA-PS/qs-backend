package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByFloorId(Long floorId);

    long countByFloorId(Long floorId);

    @Query("SELECT e.floor.id, COUNT(e) FROM Equipment e GROUP BY e.floor.id")
    List<Object[]> countEquipmentsGroupedByFloor();
}
