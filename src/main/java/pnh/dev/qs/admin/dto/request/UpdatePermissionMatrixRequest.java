package pnh.dev.qs.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePermissionMatrixRequest {
    @NotNull
    private Map<Long, List<Long>> rolePermissions;
}
