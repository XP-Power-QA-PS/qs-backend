package pnh.dev.qs.equipment.entity;

import jakarta.persistence.*;
import lombok.*;
import pnh.dev.qs.common.entity.BaseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "equipment_daily_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDailyTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    private EquipmentTestRecord testRecord;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @OneToMany(mappedBy = "dailyTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EquipmentTestAttempt> attempts = new ArrayList<>();
}

