package pnh.dev.qs.equipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TestRecordRequest {
    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;
}
