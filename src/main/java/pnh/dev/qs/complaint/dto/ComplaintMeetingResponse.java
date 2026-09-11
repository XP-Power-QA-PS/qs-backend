package pnh.dev.qs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintMeetingResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String trackingNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meetingDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private String roomLocation;
    private String agenda;
    private String organizerEmail;
    private String organizerName;
    private String recipientsJson;
    private Instant sentAt;
    private String minutes;
    private String conclusion;
    private String agreedContainment;
    private Boolean isConcluded;
    private Instant concludedAt;
    private String concludedBy;
}
