package pnh.dev.qs.equipment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import pnh.dev.qs.common.entity.AuditableEntity;

@Entity
@Table(name = "floors")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
public class Floor extends AuditableEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;
}
