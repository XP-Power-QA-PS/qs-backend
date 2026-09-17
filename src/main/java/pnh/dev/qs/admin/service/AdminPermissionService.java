package pnh.dev.qs.admin.service;

import pnh.dev.qs.admin.dto.request.UpdatePermissionMatrixRequest;
import pnh.dev.qs.admin.dto.response.PermissionMatrixResponse;

public interface AdminPermissionService {
    PermissionMatrixResponse getPermissionMatrix();
    PermissionMatrixResponse updatePermissionMatrix(UpdatePermissionMatrixRequest request);
}
