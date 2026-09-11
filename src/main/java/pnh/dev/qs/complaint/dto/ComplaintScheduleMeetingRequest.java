package pnh.dev.qs.complaint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pnh.dev.qs.user.dto.EmailRecipientDTO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintScheduleMeetingRequest {

    @NotNull(message = "Meeting date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate meetingDate;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "Room location or meeting link is required")
    private String roomLocation;

    @NotBlank(message = "Meeting agenda is required")
    private String agenda;

    @NotEmpty(message = "At least one recipient must be specified")
    @Valid
    private List<EmailRecipientDTO> recipients;
}
