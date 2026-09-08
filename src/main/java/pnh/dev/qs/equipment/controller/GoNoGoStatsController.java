package pnh.dev.qs.equipment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pnh.dev.qs.equipment.dto.stats.*;
import pnh.dev.qs.equipment.service.GoNoGoStatsService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * REST controller cung cấp các endpoint thống kê cho chức năng GONOGO.
 * Tất cả các role đều có quyền truy cập.
 */
@RestController
@RequestMapping("/api/stats/gonogo")
@RequiredArgsConstructor
public class GoNoGoStatsController {

    private final GoNoGoStatsService statsService;

    /**
     * Helper: lấy ngày đầu/cuối tháng nếu client không truyền date range.
     */
    private LocalDate defaultStart(LocalDate start) {
        if (start != null) return start;
        return YearMonth.now().atDay(1);
    }

    private LocalDate defaultEnd(LocalDate end) {
        if (end != null) return end;
        return YearMonth.now().atEndOfMonth();
    }

    // ─── 1. Summary KPI ──────────────────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/summary
     *
     * @param startDate Ngày bắt đầu (YYYY-MM-DD), mặc định đầu tháng hiện tại
     * @param endDate   Ngày kết thúc (YYYY-MM-DD), mặc định cuối tháng hiện tại
     * @param floorId   Lọc theo Floor (null = tất cả)
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<GoNoGoSummaryDTO> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long floorId) {
        return ResponseEntity.ok(statsService.getSummary(defaultStart(startDate), defaultEnd(endDate), floorId));
    }

    // ─── 2. Daily Trend ───────────────────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/trend
     * Xu hướng từng ngày (Line Chart + Stacked Bar Chart).
     */
    @GetMapping("/trend")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<DailyTrendDTO>> getDailyTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long floorId) {
        return ResponseEntity.ok(statsService.getDailyTrend(defaultStart(startDate), defaultEnd(endDate), floorId));
    }

    // ─── 3. Defect Breakdown ─────────────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/defect-breakdown
     * Phân rã nguyên nhân lỗi Go / No-Go / Program (Donut Chart).
     */
    @GetMapping("/defect-breakdown")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<DefectBreakdownDTO> getDefectBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long floorId) {
        return ResponseEntity.ok(statsService.getDefectBreakdown(defaultStart(startDate), defaultEnd(endDate), floorId));
    }

    // ─── 4. By Floor ──────────────────────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/by-floor
     * So sánh pass rate giữa các Floor (Horizontal Bar Chart).
     */
    @GetMapping("/by-floor")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<FloorStatsDTO>> getStatsByFloor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(statsService.getStatsByFloor(defaultStart(startDate), defaultEnd(endDate)));
    }

    // ─── 5. Worst Equipments ─────────────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/worst-equipments
     * Top N thiết bị hay lỗi nhất (Table).
     *
     * @param limit Số kết quả tối đa, mặc định 10
     */
    @GetMapping("/worst-equipments")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EquipmentRankDTO>> getWorstEquipments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(statsService.getWorstEquipments(defaultStart(startDate), defaultEnd(endDate), limit));
    }

    // ─── 6. Per-Equipment Analytics ───────────────────────────────────────────

    /**
     * GET /api/stats/gonogo/equipment/{equipmentId}
     * Toàn bộ analytics cho 1 thiết bị cụ thể.
     *
     * @param equipmentId ID thiết bị
     * @param month       Tháng cần xem chi tiết (1–12), mặc định tháng hiện tại
     * @param year        Năm cần xem chi tiết, mặc định năm hiện tại
     */
    @GetMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<EquipmentStatsDTO> getEquipmentStats(
            @PathVariable Long equipmentId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(statsService.getEquipmentStats(equipmentId, month, year));
    }
}
