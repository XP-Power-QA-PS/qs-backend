package pnh.dev.qs.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pnh.dev.qs.admin.dto.request.FloorRequest;
import pnh.dev.qs.admin.dto.response.FloorResponse;
import pnh.dev.qs.admin.service.AdminFloorService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/floors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminFloorController {

    private final AdminFloorService adminFloorService;

    @GetMapping
    public ResponseEntity<List<FloorResponse>> getAllFloors() {
        return ResponseEntity.ok(adminFloorService.getAllFloors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FloorResponse> getFloorById(@PathVariable Long id) {
        return ResponseEntity.ok(adminFloorService.getFloorById(id));
    }

    @PostMapping
    public ResponseEntity<FloorResponse> createFloor(@Valid @RequestBody FloorRequest request) {
        return new ResponseEntity<>(adminFloorService.createFloor(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FloorResponse> updateFloor(@PathVariable Long id, @Valid @RequestBody FloorRequest request) {
        return ResponseEntity.ok(adminFloorService.updateFloor(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFloor(@PathVariable Long id) {
        adminFloorService.deleteFloor(id);
        return ResponseEntity.noContent().build();
    }
}
