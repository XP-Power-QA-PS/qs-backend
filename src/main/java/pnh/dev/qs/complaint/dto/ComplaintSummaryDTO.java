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
public class ComplaintSummaryDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String trackingNo;
    private String controlNo;
    private Integer year;
    private String month;
    private Integer week;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate receivedDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private String customerName;
    private String model;
    private String issueDescription;
    private String defectCategory;
    private String defectName;
    private Integer quantity;
    private InternalExternal internalExternal;
    private ComplaintStatus status;
    private String actionStatus;
    private String finalStatus;
    private Long ageingOpen;
    private Long ageingClosed;
}
