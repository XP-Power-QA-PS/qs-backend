package pnh.dev.qs.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import pnh.dev.qs.equipment.enums.TestStatus;
import java.time.Instant;

@Data
@Builder
public class EquipmentTestAttemptDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private Instant attemptTime;
    private TestStatus programStatus;
    private TestStatus goStatus;
    private TestStatus noGoStatus;
    private TestStatus resultStatus;
    private String remark;
    private String testerUsername;
}

