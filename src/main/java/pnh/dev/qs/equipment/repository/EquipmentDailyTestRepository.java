package pnh.dev.qs.equipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pnh.dev.qs.equipment.entity.EquipmentDailyTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentDailyTestRepository extends JpaRepository<EquipmentDailyTest, Long> {
    List<EquipmentDailyTest> findByTestRecordIdOrderByTestDateDesc(Long recordId);
    Optional<EquipmentDailyTest> findByTestRecordIdAndTestDate(Long recordId, LocalDate testDate);
}

