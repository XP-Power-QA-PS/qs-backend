package pnh.dev.qs.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendResultDTO {
    private String trackingNo;
    private int totalRecipients;
    private int toCount;
    private int ccCount;
    private String message;
    private boolean async;
    private Instant timestamp;
}
