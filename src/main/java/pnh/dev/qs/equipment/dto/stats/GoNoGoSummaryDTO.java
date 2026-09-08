package pnh.dev.qs.equipment.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoNoGoSummaryDTO {

    /** Tổng số lượt thử trong kỳ */
    private long totalAttempts;

    /** Tổng số lượt PASS */
    private long passCount;

    /** Tổng số lượt FAIL */
    private long failCount;

    /** Tỷ lệ pass (0–100) */
    private double passRate;

    /** Số lượt mà goStatus = FAIL */
    private long goFailCount;

    /** Số lượt mà noGoStatus = FAIL */
    private long noGoFailCount;

    /** Số lượt mà programStatus = FAIL */
    private long programFailCount;

    /** Số thiết bị có ít nhất 1 attempt FAIL trong kỳ */
    private long criticalEquipmentCount;

    /** Số thiết bị đã được test ít nhất 1 lần trong kỳ */
    private long testedEquipmentCount;
}
