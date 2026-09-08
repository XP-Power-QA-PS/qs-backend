package pnh.dev.qs.equipment.service;

import pnh.dev.qs.equipment.dto.stats.*;

import java.time.LocalDate;
import java.util.List;

public interface GoNoGoStatsService {

    /**
     * KPI tổng hợp cho toàn hệ thống hoặc theo floor.
     *
     * @param startDate Ngày bắt đầu (inclusive)
     * @param endDate   Ngày kết thúc (inclusive)
     * @param floorId   null = tất cả floor
     */
    GoNoGoSummaryDTO getSummary(LocalDate startDate, LocalDate endDate, Long floorId);

    /**
     * Xu hướng theo từng ngày (Line + Stacked Bar).
     */
    List<DailyTrendDTO> getDailyTrend(LocalDate startDate, LocalDate endDate, Long floorId);

    /**
     * Phân rã nguyên nhân lỗi Go/NoGo/Program (Donut chart).
     */
    DefectBreakdownDTO getDefectBreakdown(LocalDate startDate, LocalDate endDate, Long floorId);

    /**
     * Thống kê pass rate theo từng Floor (Horizontal Bar chart).
     */
    List<FloorStatsDTO> getStatsByFloor(LocalDate startDate, LocalDate endDate);

    /**
     * Top N thiết bị có nhiều lỗi nhất.
     *
     * @param limit Số lượng kết quả trả về (mặc định 10)
     */
    List<EquipmentRankDTO> getWorstEquipments(LocalDate startDate, LocalDate endDate, int limit);

    /**
     * Toàn bộ analytics cho 1 thiết bị.
     *
     * @param equipmentId  ID thiết bị
     * @param month        Tháng cần xem chi tiết (daily breakdown), null = tháng hiện tại
     * @param year         Năm cần xem chi tiết, null = năm hiện tại
     */
    EquipmentStatsDTO getEquipmentStats(Long equipmentId, Integer month, Integer year);
}
