package pnh.dev.qs.equipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

@Data
public class FloorDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String name;
    private String description;
}
