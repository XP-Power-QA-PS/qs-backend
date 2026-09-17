package pnh.dev.qs.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMatrixModuleGroup {
    private String moduleKey;
    private String moduleName;
    private String description;
    private List<PermissionItemResponse> permissions;
}
