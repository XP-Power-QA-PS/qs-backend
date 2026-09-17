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
import java.util.List;

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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;
    private String customerFinding;
    private String model;
    private String issueDescription;
    private String defectCategory;
    private String defectName;
    private Integer quantity;
    private String serialNumbers;
    private String pictureUrls;
    private List<String> pictureTmpKeys;

    // Phase 2: Assignment
    private String assignedTeam;
    private String assignedPerson;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate assignmentDeadline;

    // Phase 3: Containment
    private String containmentAction;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate containmentDueDate;
    private String containmentOwner;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate containmentCompletionDate;
    private String containmentStatus;

    // Phase 4: Root Cause
    private String rootCause;
    private String rootCauseCategory;
    private String rootCauseOwner;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate rootCauseCompletionDate;

    // Phase 5 & 6: CAPA
    private String correctiveAction;
    private String preventiveAction;
    private String correctivePreventiveAction;
    private String actionOwner;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actionDueDate;
    private String actionStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate capaCompletionDate;

    // Phase 7: 30-Day Effectiveness Verification
    private String effectivenessStatus;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate effectivenessVerifiedDate;
    private String effectivenessVerifiedBy;
    private String effectivenessRemarks;

    // Phase 8: Closure
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closureDate;
    private String finalStatus;
    private String finalEvidence;
    private String finalEvidenceTmpKey;
    private String remarks;

    // Lifecycle Status override / transition
    private ComplaintStatus status;
}
