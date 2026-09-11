package pnh.dev.qs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.enums.InternalExternal;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintUpdateRequest {

    private String controlNo;
    private String buildingStage;
    private String capaNo;
    private String originOfComplaint;
    private InternalExternal internalExternal;
    private String salesforceCapa;
    private String area;
    private String customerName;
    private String customerFinding;
    private String model;
    private String issueDescription;
    private String defectCategory;
    private String defectName;
    private Integer quantity;
    private String serialNumbers;
    private String pictureUrls;

    // Phase 3: Containment
    private String containmentAction;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate containmentDueDate;

    // Phase 4: Root Cause
    private String rootCause;

    // Phase 5 & 6: CAPA
    private String correctivePreventiveAction;
    private String actionOwner;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDueDate;
    private String actionStatus;

    // Phase 7: Closure
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closureDate;
    private String finalStatus;
    private String remarks;

    // Lifecycle Status override / transition
    private ComplaintStatus status;
}
