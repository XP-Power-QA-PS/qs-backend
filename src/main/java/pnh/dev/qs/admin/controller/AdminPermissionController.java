package pnh.dev.qs.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pnh.dev.qs.admin.dto.request.UpdatePermissionMatrixRequest;
import pnh.dev.qs.admin.dto.response.PermissionMatrixResponse;
import pnh.dev.qs.admin.service.AdminPermissionService;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPermissionController {

    private final AdminPermissionService adminPermissionService;

    @GetMapping("/matrix")
    public ResponseEntity<PermissionMatrixResponse> getPermissionMatrix() {
        return ResponseEntity.ok(adminPermissionService.getPermissionMatrix());
    }

    @PutMapping("/matrix")
    public ResponseEntity<PermissionMatrixResponse> updatePermissionMatrix(
            @Valid @RequestBody UpdatePermissionMatrixRequest request) {
        return ResponseEntity.ok(adminPermissionService.updatePermissionMatrix(request));
    }
}
