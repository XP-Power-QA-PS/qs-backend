package pnh.dev.qs.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import pnh.dev.qs.common.entity.AuditableEntity;

@Entity
@Table(name = "equipments")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Equipment extends AuditableEntity {

    @Column(name = "equipment_code", nullable = false, unique = true, length = 100)
    private String equipmentCode;

    @Column(name = "equipment_name", nullable = false, length = 200)
    private String equipmentName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;
}
