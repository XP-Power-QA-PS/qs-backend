package pnh.dev.qs.admin.service;

import pnh.dev.qs.admin.dto.request.FloorRequest;
import pnh.dev.qs.admin.dto.response.FloorResponse;

import java.util.List;

public interface AdminFloorService {

    List<FloorResponse> getAllFloors();

    FloorResponse getFloorById(Long id);

    FloorResponse createFloor(FloorRequest request);

    FloorResponse updateFloor(Long id, FloorRequest request);

    void deleteFloor(Long id);
}
