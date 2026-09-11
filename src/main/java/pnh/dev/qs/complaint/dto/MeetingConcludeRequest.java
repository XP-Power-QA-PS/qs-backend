package pnh.dev.qs.complaint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingConcludeRequest {

    private String minutes;
    private String conclusion;
    private String agreedContainment;
    private Boolean transitionToContainment;
}
