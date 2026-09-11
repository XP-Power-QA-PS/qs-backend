package pnh.dev.qs.complaint.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import pnh.dev.qs.common.entity.AuditableEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "complaint_meetings")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintMeeting extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private CustomerComplaint complaint;

    @Column(name = "tracking_no", nullable = false, length = 30)
    private String trackingNo;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "room_location", nullable = false, length = 255)
    private String roomLocation;

    @Column(name = "agenda", nullable = false, columnDefinition = "TEXT")
    private String agenda;

    @Column(name = "organizer_email", nullable = false, length = 150)
    private String organizerEmail;

    @Column(name = "organizer_name", length = 150)
    private String organizerName;

    @Column(name = "recipients_json", nullable = false, columnDefinition = "TEXT")
    private String recipientsJson;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "minutes", columnDefinition = "TEXT")
    private String minutes;

    @Column(name = "conclusion", length = 100)
    private String conclusion;

    @Column(name = "agreed_containment", columnDefinition = "TEXT")
    private String agreedContainment;

    @Column(name = "is_concluded")
    @Builder.Default
    private Boolean isConcluded = false;

    @Column(name = "concluded_at")
    private Instant concludedAt;

    @Column(name = "concluded_by", length = 50)
    private String concludedBy;
}
