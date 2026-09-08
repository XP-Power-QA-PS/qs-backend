package pnh.dev.qs.equipment.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorStatsDTO {

    private Long floorId;

    private String floorName;

    private long totalAttempts;

    private long passCount;

    private long failCount;

    /** Tỷ lệ pass (0–100) */
    private double passRate;

    /** Số thiết bị trong floor này có test trong kỳ */
    private long testedEquipmentCount;
}
