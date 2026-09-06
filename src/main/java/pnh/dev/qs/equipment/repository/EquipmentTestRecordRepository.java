package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.EquipmentTestRecord;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTestRecordRepository extends JpaRepository<EquipmentTestRecord, Long> {
    
    Optional<EquipmentTestRecord> findByEquipmentIdAndTestMonthAndTestYear(Long equipmentId, Integer testMonth, Integer testYear);

    List<EquipmentTestRecord> findByEquipmentIdInAndTestMonthAndTestYear(List<Long> equipmentIds, Integer testMonth, Integer testYear);
}
