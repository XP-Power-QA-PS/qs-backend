package pnh.dev.qs.equipment.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import pnh.dev.qs.common.entity.BaseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment_test_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"equipment_id", "test_month", "test_year"})
})
@Getter
@Setter
public class EquipmentTestRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "test_month", nullable = false)
    private Integer testMonth;

    @Column(name = "test_year", nullable = false)
    private Integer testYear;

    @Column(name = "tested_at", nullable = false)
    private Instant testedAt;

    @OneToMany(mappedBy = "testRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipmentDailyTest> dailyTests = new ArrayList<>();
}

