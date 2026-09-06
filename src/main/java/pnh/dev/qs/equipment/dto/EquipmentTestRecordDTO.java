package pnh.dev.qs.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.Instant;

@Data
public class EquipmentTestRecordDTO {
    
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private Integer testMonth;
    private Integer testYear;
    private Instant testedAt;
}
