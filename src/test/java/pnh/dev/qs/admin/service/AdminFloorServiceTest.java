package pnh.dev.qs.admin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pnh.dev.qs.admin.dto.request.FloorRequest;
import pnh.dev.qs.admin.dto.response.FloorResponse;
import pnh.dev.qs.admin.service.impl.AdminFloorServiceImpl;
import pnh.dev.qs.equipment.entity.Floor;
import pnh.dev.qs.equipment.repository.EquipmentRepository;
import pnh.dev.qs.equipment.repository.FloorRepository;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFloorServiceTest {

    @Mock
    private FloorRepository floorRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    private AdminFloorServiceImpl adminFloorService;

    @BeforeEach
    void setUp() {
        adminFloorService = new AdminFloorServiceImpl(floorRepository, equipmentRepository);
    }

    @Test
    void getAllFloors_shouldReturnFloorsWithCounts() {
        Floor floor2 = new Floor();
        floor2.setId(2L);
        floor2.setName("2nd Floor");
        floor2.setDescription("Production Area");

        Floor floor3 = new Floor();
        floor3.setId(3L);
        floor3.setName("3rd Floor");
        floor3.setDescription("Testing Area");

        when(floorRepository.findAllByOrderByNameAsc()).thenReturn(List.of(floor2, floor3));

        List<Object[]> groupedCounts = List.of(
                new Object[]{2L, 6L},
                new Object[]{3L, 9L}
        );
        when(equipmentRepository.countEquipmentsGroupedByFloor()).thenReturn(groupedCounts);

        List<FloorResponse> result = adminFloorService.getAllFloors();

        assertEquals(2, result.size());
        assertEquals("2nd Floor", result.get(0).getName());
        assertEquals(6L, result.get(0).getEquipmentCount());
        assertEquals("3rd Floor", result.get(1).getName());
        assertEquals(9L, result.get(1).getEquipmentCount());
    }

    @Test
    void createFloor_success() {
        FloorRequest request = new FloorRequest("4th Floor", "Assembly Line 4");
        when(floorRepository.existsByNameIgnoreCase("4th Floor")).thenReturn(false);

        Floor savedFloor = new Floor();
        savedFloor.setId(400L);
        savedFloor.setName("4th Floor");
        savedFloor.setDescription("Assembly Line 4");

        when(floorRepository.save(any(Floor.class))).thenReturn(savedFloor);

        FloorResponse response = adminFloorService.createFloor(request);

        assertNotNull(response);
        assertEquals(400L, response.getId());
        assertEquals("4th Floor", response.getName());
        assertEquals("Assembly Line 4", response.getDescription());
        assertEquals(0L, response.getEquipmentCount());
    }

    @Test
    void createFloor_duplicateName_throwsDuplicateResourceException() {
        FloorRequest request = new FloorRequest("2nd Floor", "Duplicate");
        when(floorRepository.existsByNameIgnoreCase("2nd Floor")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> adminFloorService.createFloor(request));
        verify(floorRepository, never()).save(any(Floor.class));
    }

    @Test
    void updateFloor_success() {
        Floor existingFloor = new Floor();
        existingFloor.setId(2L);
        existingFloor.setName("2nd Floor");
        existingFloor.setDescription("Old Description");

        when(floorRepository.findById(2L)).thenReturn(Optional.of(existingFloor));
        when(floorRepository.existsByNameIgnoreCaseAndIdNot("2nd Floor - Renovated", 2L)).thenReturn(false);
        when(floorRepository.save(existingFloor)).thenReturn(existingFloor);
        when(equipmentRepository.countByFloorId(2L)).thenReturn(6L);

        FloorRequest updateRequest = new FloorRequest("2nd Floor - Renovated", "New Description");
        FloorResponse response = adminFloorService.updateFloor(2L, updateRequest);

        assertEquals("2nd Floor - Renovated", response.getName());
        assertEquals("New Description", response.getDescription());
        assertEquals(6L, response.getEquipmentCount());
    }

    @Test
    void updateFloor_duplicateName_throwsDuplicateResourceException() {
        Floor existingFloor = new Floor();
        existingFloor.setId(2L);
        existingFloor.setName("2nd Floor");

        when(floorRepository.findById(2L)).thenReturn(Optional.of(existingFloor));
        when(floorRepository.existsByNameIgnoreCaseAndIdNot("3rd Floor", 2L)).thenReturn(true);

        FloorRequest updateRequest = new FloorRequest("3rd Floor", "New Description");
        assertThrows(DuplicateResourceException.class, () -> adminFloorService.updateFloor(2L, updateRequest));
        verify(floorRepository, never()).save(existingFloor);
    }

    @Test
    void deleteFloor_withEquipments_throwsBadRequestException() {
        Floor existingFloor = new Floor();
        existingFloor.setId(2L);
        existingFloor.setName("2nd Floor");

        when(floorRepository.findById(2L)).thenReturn(Optional.of(existingFloor));
        when(equipmentRepository.countByFloorId(2L)).thenReturn(5L);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> adminFloorService.deleteFloor(2L));
        assertTrue(ex.getMessage().contains("currently contains 5 equipment(s)"));
        verify(floorRepository, never()).save(any());
    }

    @Test
    void deleteFloor_withoutEquipments_appliesSoftDelete() {
        Floor existingFloor = new Floor();
        existingFloor.setId(4L);
        existingFloor.setName("Empty Floor");

        when(floorRepository.findById(4L)).thenReturn(Optional.of(existingFloor));
        when(equipmentRepository.countByFloorId(4L)).thenReturn(0L);

        adminFloorService.deleteFloor(4L);

        assertNotNull(existingFloor.getDeletedAt());
        verify(floorRepository).save(existingFloor);
    }
}
