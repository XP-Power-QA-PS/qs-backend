package pnh.dev.qs.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionItemResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    private String code;
    private String module;
    private String action;
    private String name;
    private String description;
}
