package pnh.dev.qs.equipment.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectBreakdownDTO {

    /** Số lượt goStatus = FAIL */
    private long goFailCount;

    /** Số lượt noGoStatus = FAIL */
    private long noGoFailCount;

    /** Số lượt programStatus = FAIL */
    private long programFailCount;

    /** Tổng lượt FAIL (để tính %) */
    private long totalFailCount;

    /** % Go fail trong tổng fail */
    private double goFailPercent;

    /** % No-Go fail trong tổng fail */
    private double noGoFailPercent;

    /** % Program fail trong tổng fail */
    private double programFailPercent;
}
