package pnh.dev.qs.equipment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.equipment.dto.*;
import pnh.dev.qs.equipment.entity.*;
import pnh.dev.qs.equipment.repository.EquipmentRepository;
import pnh.dev.qs.equipment.repository.FloorRepository;
import pnh.dev.qs.equipment.service.EquipmentService;
import pnh.dev.qs.equipment.service.EquipmentTestEngine;
import pnh.dev.qs.user.entity.UserAccount;
import pnh.dev.qs.user.service.UserManagementService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final FloorRepository floorRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserManagementService userManagementService;
    private final EquipmentTestEngine equipmentTestEngine;

    @Override
    public List<FloorDTO> getAllFloors() {
        return floorRepository.findAll().stream()
                .map(floor -> {
                    FloorDTO dto = new FloorDTO();
                    dto.setId(floor.getId());
                    dto.setName(floor.getName());
                    dto.setDescription(floor.getDescription());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentDTO> getEquipmentsByFloor(Long floorId) {
        return equipmentRepository.findByFloorId(floorId).stream()
                .map(equipment -> {
                    EquipmentDTO dto = new EquipmentDTO();
                    dto.setId(equipment.getId());
                    dto.setEquipmentCode(equipment.getEquipmentCode());
                    dto.setEquipmentName(equipment.getEquipmentName());
                    dto.setFloorId(equipment.getFloor().getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void performTest(TestRecordRequest request, String username) {
        equipmentTestEngine.initiateMonthlyTest(request.getEquipmentId(), LocalDate.now(), username);
    }

    @Override
    public List<EquipmentTestRecordDTO> getTestHistory(Long equipmentId) {
        return equipmentTestEngine.getTestHistory(equipmentId).stream()
                .map(r -> {
                    EquipmentTestRecordDTO dto = new EquipmentTestRecordDTO();
                    dto.setId(r.getId());
                    dto.setTestMonth(r.getTestMonth());
                    dto.setTestYear(r.getTestYear());
                    dto.setTestedAt(r.getTestedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EquipmentDailyTestDTO> getDailyTests(Long recordId) {
        return equipmentTestEngine.getDailyTests(recordId).stream()
                .map(daily -> EquipmentDailyTestDTO.builder()
                        .id(daily.getId())
                        .recordId(daily.getTestRecord().getId())
                        .testDate(daily.getTestDate())
                        .attempts(daily.getAttempts().stream()
                                .map(attempt -> EquipmentTestAttemptDTO.builder()
                                        .id(attempt.getId())
                                        .attemptTime(attempt.getAttemptTime())
                                        .programStatus(attempt.getProgramStatus())
                                        .goStatus(attempt.getGoStatus())
                                        .noGoStatus(attempt.getNoGoStatus())
                                        .resultStatus(attempt.getResultStatus())
                                        .remark(attempt.getRemark())
                                        .testerUsername(attempt.getTester().getUsername())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EquipmentDailyTestDTO createDailyTest(Long recordId, String username) {
        EquipmentDailyTest dailyTest = equipmentTestEngine.createDailyTest(recordId, LocalDate.now(), username);

        return EquipmentDailyTestDTO.builder()
                .id(dailyTest.getId())
                .recordId(dailyTest.getTestRecord().getId())
                .testDate(dailyTest.getTestDate())
                .attempts(List.of())
                .build();
    }

    @Override
    @Transactional
    public EquipmentTestAttemptDTO addTestAttempt(Long dailyTestId, CreateTestAttemptRequest request, String username) {
        UserAccount tester = userManagementService.getUserByUsername(username);

        EquipmentTestAttempt attempt = equipmentTestEngine.recordTestAttempt(
                dailyTestId,
                request.getProgramStatus(),
                request.getGoStatus(),
                request.getNoGoStatus(),
                request.getRemark(),
                tester
        );

        return EquipmentTestAttemptDTO.builder()
                .id(attempt.getId())
                .attemptTime(attempt.getAttemptTime())
                .programStatus(attempt.getProgramStatus())
                .goStatus(attempt.getGoStatus())
                .noGoStatus(attempt.getNoGoStatus())
                .resultStatus(attempt.getResultStatus())
                .remark(attempt.getRemark())
                .testerUsername(attempt.getTester().getUsername())
                .build();
    }
}


