package pnh.dev.qs.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pnh.dev.qs.common.entity.BaseEntity;

@Entity
@Table(name = "roles")
@Getter
@Setter
public class Role extends BaseEntity {
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    private String description;
}
