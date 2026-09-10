package pnh.dev.qs.equipment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pnh.dev.qs.equipment.dto.*;
import pnh.dev.qs.equipment.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/api/equipments")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/floors")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<FloorDTO>> getAllFloors() {

        return ResponseEntity.ok(equipmentService.getAllFloors());
    }

    @GetMapping("/floors/{floorId}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentsByFloor(@PathVariable Long floorId) {
        return ResponseEntity.ok(equipmentService.getEquipmentsByFloor(floorId));
    }

    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> performTest(@Valid @RequestBody TestRecordRequest request, Authentication authentication) {
        equipmentService.performTest(request, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{equipmentId}/tests")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EquipmentTestRecordDTO>> getTestHistory(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(equipmentService.getTestHistory(equipmentId));
    }

    @GetMapping("/records/{recordId}/daily-tests")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<EquipmentDailyTestDTO>> getDailyTests(@PathVariable Long recordId) {
        return ResponseEntity.ok(equipmentService.getDailyTests(recordId));
    }

    @PostMapping("/records/{recordId}/daily-tests")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<EquipmentDailyTestDTO> createDailyTest(@PathVariable Long recordId, Authentication authentication) {
        return ResponseEntity.ok(equipmentService.createDailyTest(recordId, authentication.getName()));
    }

    @PostMapping("/daily-tests/{dailyTestId}/attempts")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<EquipmentTestAttemptDTO> addTestAttempt(@PathVariable Long dailyTestId, @Valid @RequestBody CreateTestAttemptRequest request, Authentication authentication) {
        return ResponseEntity.ok(equipmentService.addTestAttempt(dailyTestId, request, authentication.getName()));
    }


    @GetMapping("/records/{recordId}/compare-days")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<pnh.dev.qs.equipment.dto.DayComparisonDTO> compareDays(
            @PathVariable Long recordId,
            @RequestParam List<Long> dayIds) {
        return ResponseEntity.ok(equipmentService.compareDays(recordId, dayIds));
    }
}

