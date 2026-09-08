package pnh.dev.qs.equipment.dto.stats;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendDTO {

    /** Ngày test */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate testDate;

    /** Tổng lượt thử trong ngày */
    private long totalAttempts;

    /** Số lượt PASS trong ngày */
    private long passCount;

    /** Số lượt FAIL trong ngày */
    private long failCount;

    /** Tỷ lệ pass (0–100) */
    private double passRate;
}
