package pnh.dev.qs.equipment.dto.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Per-equipment analytics DTO.
 * Gom tất cả dữ liệu cần thiết cho EquipmentAnalyticsPage vào 1 response duy nhất.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentStatsDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long equipmentId;

    private String equipmentCode;

    private String equipmentName;

    private String floorName;

    /** KPI tổng quan của thiết bị trong kỳ */
    private GoNoGoSummaryDTO summary;

    /**
     * Xu hướng Pass Rate theo tháng (mỗi phần tử = 1 tháng test record).
     * Dùng cho biểu đồ Line Chart Monthly Trend.
     */
    private List<MonthlyStatsDTO> monthlyTrend;

    /**
     * Chi tiết từng ngày trong tháng được chọn (default = tháng hiện tại).
     * Dùng cho Grouped Bar Chart và Scatter Plot.
     */
    private List<DailyTrendDTO> dailyBreakdown;

    /**
     * Phân tích nguyên nhân lỗi của thiết bị này.
     */
    private DefectBreakdownDTO defectBreakdown;

    // ─── Inner DTO ───────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatsDTO {

        private int month;

        private int year;

        /** Label hiển thị, ví dụ "09/2026" */
        private String label;

        private long totalAttempts;

        private long passCount;

        private long failCount;

        private double passRate;

        /** Số ngày test trong tháng đó */
        private long totalDays;

        /** Trung bình số lần thử per ngày */
        private double avgAttemptsPerDay;
    }
}
