package pnh.dev.qs.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayComparisonDTO {
    private List<DailySummaryDTO> days;
    private ComparisonInsightDTO insights;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailySummaryDTO {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private Long dailyTestId;
        private LocalDate testDate;
        private int totalAttempts;
        private int passCount;
        private int failCount;
        private double passRate;
        private String overallStatus;
        private String latestTester;
        private List<EquipmentTestAttemptDTO> attempts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonInsightDTO {
        private boolean sameTester;
        private List<String> allTesters;
        private double passRateDifference;
        private List<String> parameterDifferences;
        private String summaryText;
    }
}
