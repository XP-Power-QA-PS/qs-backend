package pnh.dev.qs.equipment.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.equipment.dto.stats.*;
import pnh.dev.qs.equipment.entity.Equipment;
import pnh.dev.qs.equipment.repository.EquipmentRepository;
import pnh.dev.qs.equipment.repository.GoNoGoStatsRepository;
import pnh.dev.qs.equipment.service.GoNoGoStatsService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoNoGoStatsServiceImpl implements GoNoGoStatsService {

    private final GoNoGoStatsRepository statsRepository;
    private final EquipmentRepository equipmentRepository;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Instant toStartInstant(LocalDate date) {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant toEndInstant(LocalDate date) {
        return date.atTime(23, 59, 59, 999_999_999).atOffset(ZoneOffset.UTC).toInstant();
    }

    private LocalDate toLocalDate(Object raw) {
        return switch (raw) {
            case null -> null;
            case LocalDate ld -> ld;
            case java.sql.Date d -> d.toLocalDate();
            case java.util.Date d -> d.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            default -> LocalDate.parse(raw.toString());
        };
    }

    private long toLong(Object raw) {
        return switch (raw) {
            case null -> 0L;
            case BigInteger bi -> bi.longValue();
            case Long l -> l;
            case Integer i -> i.longValue();
            case BigDecimal bd -> bd.longValue();
            default -> Long.parseLong(raw.toString());
        };
    }

    private double toPassRate(long pass, long total) {
        if (total == 0) return 0.0;
        return Math.round((pass * 100.0 / total) * 10.0) / 10.0;
    }

    /**
     * Trích row đầu tiên từ List<Object[]>. Nếu không có data trả về array null-safe.
     */
    private Object[] firstRowOrEmpty(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            Object[] empty = new Object[6];
            for (int i = 0; i < 6; i++) empty[i] = 0L;
            return empty;
        }
        return rows.getFirst();
    }

    // ─── 1. Summary ───────────────────────────────────────────────────────────

    @Override
    public GoNoGoSummaryDTO getSummary(LocalDate startDate, LocalDate endDate, Long floorId) {
        Instant start = toStartInstant(startDate);
        Instant end   = toEndInstant(endDate);

        // Tách query theo floorId để tránh lỗi NULL binding PostgreSQL
        List<Object[]> rawList;
        long critical;
        long tested;

        if (floorId == null) {
            rawList  = statsRepository.getRawSummaryAllFloors(start, end);
            critical = statsRepository.countCriticalEquipmentsAllFloors(start, end);
            tested   = statsRepository.countTestedEquipmentsAllFloors(start, end);
        } else {
            rawList  = statsRepository.getRawSummaryByFloor(start, end, floorId);
            critical = statsRepository.countCriticalEquipmentsByFloor(start, end, floorId);
            tested   = statsRepository.countTestedEquipmentsByFloor(start, end, floorId);
        }

        Object[] row = firstRowOrEmpty(rawList);

        long total       = toLong(row[0]);
        long pass        = toLong(row[1]);
        long fail        = toLong(row[2]);
        long goFail      = toLong(row[3]);
        long noGoFail    = toLong(row[4]);
        long programFail = toLong(row[5]);

        return GoNoGoSummaryDTO.builder()
                .totalAttempts(total)
                .passCount(pass)
                .failCount(fail)
                .passRate(toPassRate(pass, total))
                .goFailCount(goFail)
                .noGoFailCount(noGoFail)
                .programFailCount(programFail)
                .criticalEquipmentCount(critical)
                .testedEquipmentCount(tested)
                .build();
    }

    // ─── 2. Daily Trend ───────────────────────────────────────────────────────

    @Override
    public List<DailyTrendDTO> getDailyTrend(LocalDate startDate, LocalDate endDate, Long floorId) {
        Instant start = toStartInstant(startDate);
        Instant end   = toEndInstant(endDate);

        List<Object[]> rows = (floorId == null)
                ? statsRepository.getRawDailyTrendAllFloors(start, end)
                : statsRepository.getRawDailyTrendByFloor(start, end, floorId);

        List<DailyTrendDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            long total     = toLong(row[1]);
            long pass      = toLong(row[2]);
            long fail      = toLong(row[3]);

            result.add(DailyTrendDTO.builder()
                    .testDate(date)
                    .totalAttempts(total)
                    .passCount(pass)
                    .failCount(fail)
                    .passRate(toPassRate(pass, total))
                    .build());
        }
        return result;
    }

    // ─── 3. Defect Breakdown ──────────────────────────────────────────────────

    @Override
    public DefectBreakdownDTO getDefectBreakdown(LocalDate startDate, LocalDate endDate, Long floorId) {
        // Lấy summary rồi map sang Defect breakdown
        GoNoGoSummaryDTO summary = getSummary(startDate, endDate, floorId);

        long goFail      = summary.getGoFailCount();
        long noGoFail    = summary.getNoGoFailCount();
        long programFail = summary.getProgramFailCount();
        long totalFail   = summary.getFailCount();
        long totalAtt    = summary.getTotalAttempts();

        return DefectBreakdownDTO.builder()
                .goFailCount(goFail)
                .noGoFailCount(noGoFail)
                .programFailCount(programFail)
                .totalFailCount(totalFail)
                .goFailPercent(toPassRate(goFail, totalAtt))
                .noGoFailPercent(toPassRate(noGoFail, totalAtt))
                .programFailPercent(toPassRate(programFail, totalAtt))
                .build();
    }

    // ─── 4. By Floor ──────────────────────────────────────────────────────────

    @Override
    public List<FloorStatsDTO> getStatsByFloor(LocalDate startDate, LocalDate endDate) {
        List<Object[]> rows = statsRepository.getRawStatsByFloor(
                toStartInstant(startDate), toEndInstant(endDate));

        List<FloorStatsDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            long floorId          = toLong(row[0]);
            String floorName      = (String) row[1];
            long total            = toLong(row[2]);
            long pass             = toLong(row[3]);
            long fail             = toLong(row[4]);
            long testedEquipments = toLong(row[5]);

            result.add(FloorStatsDTO.builder()
                    .floorId(floorId)
                    .floorName(floorName)
                    .totalAttempts(total)
                    .passCount(pass)
                    .failCount(fail)
                    .passRate(toPassRate(pass, total))
                    .testedEquipmentCount(testedEquipments)
                    .build());
        }
        return result;
    }

    // ─── 5. Worst Equipments ─────────────────────────────────────────────────

    @Override
    public List<EquipmentRankDTO> getWorstEquipments(LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> rows = statsRepository.getRawWorstEquipments(
                toStartInstant(startDate), toEndInstant(endDate), limit);

        List<EquipmentRankDTO> result = new ArrayList<>();
        for (Object[] row : rows) {
            long eqId      = toLong(row[0]);
            String code    = (String) row[1];
            String name    = (String) row[2];
            String floor   = (String) row[3];
            long total     = toLong(row[4]);
            long pass      = toLong(row[5]);
            long fail      = toLong(row[6]);
            long totalDays = toLong(row[7]);

            result.add(EquipmentRankDTO.builder()
                    .equipmentId(eqId)
                    .equipmentCode(code)
                    .equipmentName(name)
                    .floorName(floor)
                    .totalAttempts(total)
                    .passCount(pass)
                    .failCount(fail)
                    .passRate(toPassRate(pass, total))
                    .totalDaysWithTest(totalDays)
                    .build());
        }
        return result;
    }

    // ─── 6. Per-Equipment Analytics ───────────────────────────────────────────

    @Override
    public EquipmentStatsDTO getEquipmentStats(Long equipmentId, Integer month, Integer year) {
        LocalDate now       = LocalDate.now();
        int targetMonth     = (month != null) ? month : now.getMonthValue();
        int targetYear      = (year  != null) ? year  : now.getYear();

        // ── Metadata thiết bị ──
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found: " + equipmentId));

        // ── Daily breakdown (trong tháng target, theo thiết bị này) ──
        List<Object[]> dailyRows = statsRepository.getRawDailyBreakdownForEquipment(
                equipmentId, targetMonth, targetYear);

        long eqTotal = 0, eqPass = 0, eqFail = 0,
             eqGoFail = 0, eqNoGoFail = 0, eqProgramFail = 0;
        List<DailyTrendDTO> dailyBreakdown = new ArrayList<>();

        for (Object[] row : dailyRows) {
            LocalDate date   = toLocalDate(row[0]);
            long total       = toLong(row[1]);
            long pass        = toLong(row[2]);
            long fail        = toLong(row[3]);
            long goFail      = toLong(row[4]);
            long noGoFail    = toLong(row[5]);
            long programFail = toLong(row[6]);

            eqTotal       += total;
            eqPass        += pass;
            eqFail        += fail;
            eqGoFail      += goFail;
            eqNoGoFail    += noGoFail;
            eqProgramFail += programFail;

            dailyBreakdown.add(DailyTrendDTO.builder()
                    .testDate(date)
                    .totalAttempts(total)
                    .passCount(pass)
                    .failCount(fail)
                    .passRate(toPassRate(pass, total))
                    .build());
        }

        GoNoGoSummaryDTO eqSummary = GoNoGoSummaryDTO.builder()
                .totalAttempts(eqTotal)
                .passCount(eqPass)
                .failCount(eqFail)
                .passRate(toPassRate(eqPass, eqTotal))
                .goFailCount(eqGoFail)
                .noGoFailCount(eqNoGoFail)
                .programFailCount(eqProgramFail)
                .criticalEquipmentCount(eqFail > 0 ? 1 : 0)
                .testedEquipmentCount(eqTotal > 0 ? 1 : 0)
                .build();

        // ── Defect Breakdown ──
        DefectBreakdownDTO defects = DefectBreakdownDTO.builder()
                .goFailCount(eqGoFail)
                .noGoFailCount(eqNoGoFail)
                .programFailCount(eqProgramFail)
                .totalFailCount(eqFail)
                .goFailPercent(toPassRate(eqGoFail, eqTotal))
                .noGoFailPercent(toPassRate(eqNoGoFail, eqTotal))
                .programFailPercent(toPassRate(eqProgramFail, eqTotal))
                .build();

        // ── Monthly Trend ──
        List<Object[]> monthlyRows = statsRepository.getRawMonthlyTrendForEquipment(equipmentId);
        List<EquipmentStatsDTO.MonthlyStatsDTO> monthlyTrend = new ArrayList<>();

        for (Object[] row : monthlyRows) {
            int m      = ((Number) row[0]).intValue();
            int y      = ((Number) row[1]).intValue();
            long total = toLong(row[2]);
            long pass  = toLong(row[3]);
            long fail  = toLong(row[4]);
            long days  = toLong(row[5]);

            monthlyTrend.add(EquipmentStatsDTO.MonthlyStatsDTO.builder()
                    .month(m)
                    .year(y)
                    .label(String.format("%02d/%d", m, y))
                    .totalAttempts(total)
                    .passCount(pass)
                    .failCount(fail)
                    .passRate(toPassRate(pass, total))
                    .totalDays(days)
                    .avgAttemptsPerDay(days > 0 ? Math.round((total * 10.0 / days)) / 10.0 : 0.0)
                    .build());
        }

        return EquipmentStatsDTO.builder()
                .equipmentId(equipmentId)
                .equipmentCode(equipment.getEquipmentCode())
                .equipmentName(equipment.getEquipmentName())
                .floorName(equipment.getFloor() != null ? equipment.getFloor().getName() : "")
                .summary(eqSummary)
                .monthlyTrend(monthlyTrend)
                .dailyBreakdown(dailyBreakdown)
                .defectBreakdown(defects)
                .build();
    }
}
