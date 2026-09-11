package pnh.dev.qs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pnh.dev.qs.complaint.enums.InternalExternal;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintCreateRequest {

    @NotNull(message = "Received date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;

    private String controlNo;

    @Builder.Default
    private String buildingStage = "MP";

    private String capaNo;

    @Builder.Default
    private String originOfComplaint = "Customer";

    @Builder.Default
    private InternalExternal internalExternal = InternalExternal.EXTERNAL;

    private String salesforceCapa;

    private String area;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerFinding;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Issue description is required")
    private String issueDescription;

    private String defectCategory;

    private String defectName;

    private Integer quantity;

    private String serialNumbers;

    private String pictureUrls;
}
