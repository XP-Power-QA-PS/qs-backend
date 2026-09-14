package pnh.dev.qs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.enums.InternalExternal;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String trackingNo;
    private String controlNo;
    private String buildingStage;
    private String capaNo;
    private Integer year;
    private String month;
    private Integer week;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate closureDate;

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

    // Phase 2: Assignment & Priority
    private String assignedTeam;
    private String assignedPerson;
    private String priority;
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
    private ComplaintStatus status;
    private String finalStatus;
    private String finalEvidence;
    private String remarks;

    private Long ageingOpen;
    private Long ageingClosed;

    private List<ComplaintMeetingResponse> meetings;

    private String createdBy;
    private Instant createdAt;
    private String updatedBy;
    private Instant updatedAt;
}
