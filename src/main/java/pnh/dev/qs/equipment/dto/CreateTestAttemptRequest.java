package pnh.dev.qs.equipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pnh.dev.qs.equipment.enums.TestStatus;

@Data
public class CreateTestAttemptRequest {
    @NotNull
    private TestStatus programStatus;
    @NotNull
    private TestStatus goStatus;
    @NotNull
    private TestStatus noGoStatus;
    
    private String remark;
}

