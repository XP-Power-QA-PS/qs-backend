package pnh.dev.qs.equipment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.equipment.entity.Equipment;
import pnh.dev.qs.equipment.entity.EquipmentDailyTest;
import pnh.dev.qs.equipment.entity.EquipmentTestAttempt;
import pnh.dev.qs.equipment.entity.EquipmentTestRecord;
import pnh.dev.qs.equipment.enums.TestStatus;
import pnh.dev.qs.equipment.repository.EquipmentDailyTestRepository;
import pnh.dev.qs.equipment.repository.EquipmentRepository;
import pnh.dev.qs.equipment.repository.EquipmentTestAttemptRepository;
import pnh.dev.qs.equipment.repository.EquipmentTestRecordRepository;
import pnh.dev.qs.equipment.service.EquipmentTestEngine;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;
import pnh.dev.qs.user.entity.UserAccount;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentTestEngineImpl implements EquipmentTestEngine {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTestRecordRepository testRecordRepository;
    private final EquipmentDailyTestRepository dailyTestRepository;
    private final EquipmentTestAttemptRepository testAttemptRepository;

    @Override
    @Transactional
    public EquipmentTestRecord initiateMonthlyTest(Long equipmentId, LocalDate testDate, String username) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        int currentMonth = testDate.getMonthValue();
        int currentYear = testDate.getYear();

        boolean exists = testRecordRepository.findByEquipmentIdAndTestMonthAndTestYear(
                equipment.getId(), currentMonth, currentYear).isPresent();

        if (exists) {
            throw new BadRequestException("Test record for this month already exists.");
        }

        EquipmentTestRecord record = new EquipmentTestRecord();
        record.setEquipment(equipment);
        record.setTestMonth(currentMonth);
        record.setTestYear(currentYear);
        record.setTestedAt(Instant.now());
        record.setCreatedBy(username);

        return testRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentTestRecord> getTestHistory(Long equipmentId) {
        return testRecordRepository.findAll().stream()
                .filter(r -> r.getEquipment().getId().equals(equipmentId))
                .sorted((a, b) -> {
                    if (a.getTestYear().equals(b.getTestYear())) {
                        return b.getTestMonth().compareTo(a.getTestMonth());
                    }
                    return b.getTestYear().compareTo(a.getTestYear());
                })
                .toList();
    }

    @Override
    @Transactional
    public EquipmentDailyTest createDailyTest(Long testRecordId, LocalDate testDate, String username) {
        EquipmentTestRecord record = testRecordRepository.findById(testRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Test record not found"));

        if (dailyTestRepository.findByTestRecordIdAndTestDate(testRecordId, testDate).isPresent()) {
            throw new BadRequestException("Daily test for this date already exists.");
        }

        EquipmentDailyTest dailyTest = EquipmentDailyTest.builder()
                .testRecord(record)
                .testDate(testDate)
                .build();
        dailyTest.setCreatedBy(username);

        return dailyTestRepository.save(dailyTest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentDailyTest> getDailyTests(Long testRecordId) {
        return dailyTestRepository.findByTestRecordIdOrderByTestDateDesc(testRecordId);
    }

    @Override
    @Transactional
    public EquipmentTestAttempt recordTestAttempt(Long dailyTestId, TestStatus programStatus, TestStatus goStatus, TestStatus noGoStatus, String remark, UserAccount tester) {
        EquipmentDailyTest dailyTest = dailyTestRepository.findById(dailyTestId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily test not found"));

        TestStatus resultStatus = TestStatus.FAIL;
        if (programStatus == TestStatus.PASS && goStatus == TestStatus.PASS && noGoStatus == TestStatus.FAIL) {
            resultStatus = TestStatus.PASS;
        }

        EquipmentTestAttempt attempt = EquipmentTestAttempt.builder()
                .dailyTest(dailyTest)
                .attemptTime(Instant.now())
                .programStatus(programStatus)
                .goStatus(goStatus)
                .noGoStatus(noGoStatus)
                .resultStatus(resultStatus)
                .remark(remark)
                .tester(tester)
                .build();
        attempt.setCreatedBy(tester.getUsername());

        return testAttemptRepository.save(attempt);
    }
}
