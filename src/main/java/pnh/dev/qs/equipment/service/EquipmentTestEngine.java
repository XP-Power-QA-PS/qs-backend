package pnh.dev.qs.equipment.service;

import pnh.dev.qs.equipment.entity.EquipmentDailyTest;
import pnh.dev.qs.equipment.entity.EquipmentTestAttempt;
import pnh.dev.qs.equipment.entity.EquipmentTestRecord;
import pnh.dev.qs.equipment.enums.TestStatus;
import pnh.dev.qs.user.entity.UserAccount;

import java.time.LocalDate;
import java.util.List;

public interface EquipmentTestEngine {

    EquipmentTestRecord initiateMonthlyTest(Long equipmentId, LocalDate testDate, String username);

    List<EquipmentTestRecord> getTestHistory(Long equipmentId);

    EquipmentDailyTest createDailyTest(Long testRecordId, LocalDate testDate, String username);

    List<EquipmentDailyTest> getDailyTests(Long testRecordId);

    EquipmentTestAttempt recordTestAttempt(Long dailyTestId, TestStatus programStatus, TestStatus goStatus, TestStatus noGoStatus, String remark, UserAccount tester);
}

