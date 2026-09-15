package pnh.dev.qs.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pnh.dev.qs.admin.dto.request.FloorRequest;
import pnh.dev.qs.admin.dto.response.FloorResponse;
import pnh.dev.qs.admin.service.AdminFloorService;
import pnh.dev.qs.equipment.entity.Floor;
import pnh.dev.qs.equipment.repository.EquipmentRepository;
import pnh.dev.qs.equipment.repository.FloorRepository;
import pnh.dev.qs.exception.custom.BadRequestException;
import pnh.dev.qs.exception.custom.DuplicateResourceException;
import pnh.dev.qs.exception.custom.ResourceNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFloorServiceImpl implements AdminFloorService {

    private final FloorRepository floorRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FloorResponse> getAllFloors() {
        List<Floor> floors = floorRepository.findAllByOrderByNameAsc();
        Map<Long, Long> counts = equipmentRepository.countEquipmentsGroupedByFloor().stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return floors.stream()
                .map(floor -> mapToResponse(floor, counts.getOrDefault(floor.getId(), 0L)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FloorResponse getFloorById(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));
        long count = equipmentRepository.countByFloorId(id);
        return mapToResponse(floor, count);
    }

    @Override
    @Transactional
    public FloorResponse createFloor(FloorRequest request) {
        String trimmedName = request.getName().trim();

        if (floorRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Floor with name '" + trimmedName + "' already exists");
        }

        Floor floor = new Floor();
        floor.setName(trimmedName);
        floor.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Floor saved = floorRepository.save(floor);
        return mapToResponse(saved, 0L);
    }

    @Override
    @Transactional
    public FloorResponse updateFloor(Long id, FloorRequest request) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));

        String trimmedName = request.getName().trim();

        if (floorRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Floor with name '" + trimmedName + "' already exists");
        }

        floor.setName(trimmedName);
        floor.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        Floor saved = floorRepository.save(floor);
        long equipmentCount = equipmentRepository.countByFloorId(id);
        return mapToResponse(saved, equipmentCount);
    }

    @Override
    @Transactional
    public void deleteFloor(Long id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Floor not found with id: " + id));

        long equipmentCount = equipmentRepository.countByFloorId(id);
        if (equipmentCount > 0) {
            throw new BadRequestException("Cannot delete floor '" + floor.getName() + "' because it currently contains "
                    + equipmentCount + " equipment(s). Please reassign or remove all equipments from this floor first.");
        }

        floor.setDeletedAt(Instant.now());
        floorRepository.save(floor);
    }

    private FloorResponse mapToResponse(Floor floor, long equipmentCount) {
        return FloorResponse.builder()
                .id(floor.getId())
                .name(floor.getName())
                .description(floor.getDescription())
                .equipmentCount(equipmentCount)
                .createdAt(floor.getCreatedAt())
                .createdBy(floor.getCreatedBy())
                .updatedAt(floor.getUpdatedAt())
                .updatedBy(floor.getUpdatedBy())
                .build();
    }
}
