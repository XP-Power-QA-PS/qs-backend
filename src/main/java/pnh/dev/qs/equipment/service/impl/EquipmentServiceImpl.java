package pnh.dev.qs.equipment.service.impl;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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

import pnh.dev.qs.equipment.enums.TestStatus;

import java.time.LocalDate;
import java.util.*;
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

    @Override
    public DayComparisonDTO compareDays(Long recordId, List<Long> dayIds) {
        if (dayIds == null || dayIds.isEmpty()) {
            throw new IllegalArgumentException("At least one day ID must be provided for comparison.");
        }

        List<EquipmentDailyTest> allDailyTests = equipmentTestEngine.getDailyTests(recordId);
        List<EquipmentDailyTest> targetDays = allDailyTests.stream()
                .filter(d -> dayIds.contains(d.getId()))
                .sorted(Comparator.comparing(EquipmentDailyTest::getTestDate))
                .toList();

        if (targetDays.isEmpty()) {
            throw new IllegalArgumentException("No matching daily tests found for the given record and day IDs.");
        }

        List<DayComparisonDTO.DailySummaryDTO> daySummaries = new ArrayList<>();
        Set<String> allTesters = new HashSet<>();

        for (EquipmentDailyTest dt : targetDays) {
            List<EquipmentTestAttempt> attempts = dt.getAttempts();
            int total = attempts.size();
            int passCount = (int) attempts.stream().filter(a -> a.getResultStatus() == TestStatus.PASS).count();
            int failCount = total - passCount;
            double passRate = total > 0 ? ((double) passCount / total) * 100.0 : 0.0;

            String overall = total == 0 ? "NO_ATTEMPTS" : (failCount == 0 ? "ALL_PASS" : "HAS_FAIL");
            String latestTester = attempts.isEmpty() ? "N/A" : attempts.getLast().getTester().getUsername();

            attempts.forEach(a -> {
                if (a.getTester() != null) {
                    allTesters.add(a.getTester().getUsername());
                }
            });

            List<EquipmentTestAttemptDTO> attemptDTOs = attempts.stream()
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
                    .collect(Collectors.toList());

            daySummaries.add(DayComparisonDTO.DailySummaryDTO.builder()
                    .dailyTestId(dt.getId())
                    .testDate(dt.getTestDate())
                    .totalAttempts(total)
                    .passCount(passCount)
                    .failCount(failCount)
                    .passRate(Math.round(passRate * 10.0) / 10.0)
                    .overallStatus(overall)
                    .latestTester(latestTester)
                    .attempts(attemptDTOs)
                    .build());
        }

        double passRateDiff = 0.0;
        if (daySummaries.size() >= 2) {
            passRateDiff = Math.abs(daySummaries.get(0).getPassRate() - daySummaries.get(1).getPassRate());
        }

        List<String> paramDifferences = getStrings(daySummaries);

        boolean sameTester = allTesters.size() == 1;
        String summaryText = daySummaries.size() >= 2
                ? String.format("Comparison between %d days: Pass rate difference %.1f%%.", daySummaries.size(), passRateDiff)
                : "Daily test details.";

        DayComparisonDTO.ComparisonInsightDTO insights = DayComparisonDTO.ComparisonInsightDTO.builder()
                .sameTester(sameTester)
                .allTesters(new ArrayList<>(allTesters))
                .passRateDifference(Math.round(passRateDiff * 10.0) / 10.0)
                .parameterDifferences(paramDifferences)
                .summaryText(summaryText)
                .build();

        return DayComparisonDTO.builder()
                .days(daySummaries)
                .insights(insights)
                .build();
    }

    private static @NonNull List<String> getStrings(List<DayComparisonDTO.DailySummaryDTO> daySummaries) {
        List<String> paramDifferences = new ArrayList<>();
        if (daySummaries.size() == 2) {
            DayComparisonDTO.DailySummaryDTO d1 = daySummaries.get(0);
            DayComparisonDTO.DailySummaryDTO d2 = daySummaries.get(1);
            if (!d1.getOverallStatus().equals(d2.getOverallStatus())) {
                paramDifferences.add(String.format("Overall status difference: Date %s (%s) vs Date %s (%s)",
                        d1.getTestDate(), d1.getOverallStatus(), d2.getTestDate(), d2.getOverallStatus()));
            }
            if (d1.getTotalAttempts() != d2.getTotalAttempts()) {
                paramDifferences.add(String.format("Total attempts difference: %s (%d attempts) vs %s (%d attempts)",
                        d1.getTestDate(), d1.getTotalAttempts(), d2.getTestDate(), d2.getTotalAttempts()));
            }
            if (!d1.getLatestTester().equals(d2.getLatestTester())) {
                paramDifferences.add(String.format("Different technicians: %s (%s) vs %s (%s)",
                        d1.getTestDate(), d1.getLatestTester(), d2.getTestDate(), d2.getLatestTester()));
            }
        }
        return paramDifferences;
    }
}


