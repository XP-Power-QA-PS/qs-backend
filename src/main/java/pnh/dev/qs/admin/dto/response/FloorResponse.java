package pnh.dev.qs.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private String description;

    private long equipmentCount;

    private Instant createdAt;

    private String createdBy;

    private Instant updatedAt;

    private String updatedBy;
}
