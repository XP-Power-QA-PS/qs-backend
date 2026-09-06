package pnh.dev.qs.equipment.dto;

import lombok.Data;

@Data
public class EquipmentDTO {
    private Long id;
    private String equipmentCode;
    private String equipmentName;
    private Long floorId;
    private boolean isTestedThisMonth;
}
