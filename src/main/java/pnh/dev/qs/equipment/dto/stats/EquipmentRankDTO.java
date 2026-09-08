package pnh.dev.qs.equipment.dto.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRankDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long equipmentId;

    private String equipmentCode;

    private String equipmentName;

    private String floorName;

    private long totalAttempts;

    private long passCount;

    private long failCount;

    /** Tỷ lệ pass (0–100) */
    private double passRate;

    /** Số ngày test (daily tests) trong kỳ */
    private long totalDaysWithTest;
}
