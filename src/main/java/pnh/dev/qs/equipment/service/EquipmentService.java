package pnh.dev.qs.equipment.service;

import pnh.dev.qs.equipment.dto.EquipmentDTO;
import pnh.dev.qs.equipment.dto.FloorDTO;
import pnh.dev.qs.equipment.dto.TestRecordRequest;
import pnh.dev.qs.equipment.dto.EquipmentTestRecordDTO;
import pnh.dev.qs.equipment.dto.EquipmentDailyTestDTO;
import pnh.dev.qs.equipment.dto.EquipmentTestAttemptDTO;
import pnh.dev.qs.equipment.dto.CreateTestAttemptRequest;

import pnh.dev.qs.equipment.dto.DayComparisonDTO;

import java.util.List;

public interface EquipmentService {
    List<FloorDTO> getAllFloors();
    List<EquipmentDTO> getEquipmentsByFloor(Long floorId);
    void performTest(TestRecordRequest request, String username);
    List<EquipmentTestRecordDTO> getTestHistory(Long equipmentId);
    List<EquipmentDailyTestDTO> getDailyTests(Long recordId);
    EquipmentDailyTestDTO createDailyTest(Long recordId, String username);
    EquipmentTestAttemptDTO addTestAttempt(Long dailyTestId, CreateTestAttemptRequest request, String username);
    DayComparisonDTO compareDays(Long recordId, List<Long> dayIds);
}
