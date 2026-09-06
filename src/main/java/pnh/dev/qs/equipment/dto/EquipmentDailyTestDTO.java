package pnh.dev.qs.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class EquipmentDailyTestDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long recordId;
    private LocalDate testDate;
    private List<EquipmentTestAttemptDTO> attempts;
}

