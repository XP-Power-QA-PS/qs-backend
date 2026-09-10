package pnh.dev.qs.equipment.dto;

import jakarta.validation.constraints.AssertTrue;
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
    
    @NotNull(message = "Machine verification confirmation is required")
    @AssertTrue(message = "You must confirm that the correct GO / NO GO machine is selected")
    private Boolean confirmedMachineCheck;

    private String remark;
}


