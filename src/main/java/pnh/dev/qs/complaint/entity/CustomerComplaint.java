package pnh.dev.qs.complaint.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import pnh.dev.qs.common.entity.AuditableEntity;
import pnh.dev.qs.complaint.enums.ComplaintStatus;
import pnh.dev.qs.complaint.enums.InternalExternal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_complaints")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerComplaint extends AuditableEntity {

    @Column(name = "tracking_no", nullable = false, unique = true, length = 30)
    private String trackingNo;

    @Column(name = "control_no", length = 50)
    private String controlNo;

    @Column(name = "building_stage", length = 50)
    private String buildingStage;

    @Column(name = "capa_no", length = 50)
    private String capaNo;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "month", nullable = false, length = 20)
    private String month;

    @Column(name = "week")
    private Integer week;

    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "closure_date")
    private LocalDate closureDate;

    @Column(name = "origin_of_complaint", length = 100)
    private String originOfComplaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "internal_external", nullable = false, length = 20)
    @Builder.Default
    private InternalExternal internalExternal = InternalExternal.EXTERNAL;

    @Column(name = "salesforce_capa", length = 100)
    private String salesforceCapa;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_finding", columnDefinition = "TEXT")
    private String customerFinding;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @Column(name = "defect_category", length = 100)
    private String defectCategory;

    @Column(name = "defect_name", length = 100)
    private String defectName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "serial_numbers", columnDefinition = "TEXT")
    private String serialNumbers;

    @Column(name = "picture_urls", columnDefinition = "TEXT")
    private String pictureUrls;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "containment_action", columnDefinition = "TEXT")
    private String containmentAction;

    @Column(name = "containment_due_date")
    private LocalDate containmentDueDate;

    @Column(name = "corrective_preventive_action", columnDefinition = "TEXT")
    private String correctivePreventiveAction;

    @Column(name = "action_owner", length = 100)
    private String actionOwner;

    @Column(name = "action_due_date")
    private LocalDate actionDueDate;

    @Column(name = "action_status", length = 50)
    @Builder.Default
    private String actionStatus = "OPEN";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.RECEIVED;

    @Column(name = "final_status", length = 50)
    private String finalStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt DESC")
    @Builder.Default
    private List<ComplaintMeeting> meetings = new ArrayList<>();
}
