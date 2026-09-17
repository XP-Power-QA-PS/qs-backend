package pnh.dev.qs.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMatrixResponse {
    private List<PermissionMatrixModuleGroup> modules;
    private List<RoleResponse> roles;
    private Map<String, Set<String>> rolePermissions;
}
