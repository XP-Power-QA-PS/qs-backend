package pnh.dev.qs.equipment.entity;

import jakarta.persistence.*;
import lombok.*;
import pnh.dev.qs.common.entity.BaseEntity;
import pnh.dev.qs.equipment.enums.TestStatus;
import pnh.dev.qs.user.entity.UserAccount;

import java.time.Instant;

@Entity
@Table(name = "equipment_test_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentTestAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_test_id", nullable = false)
    private EquipmentDailyTest dailyTest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tester_id", nullable = false)
    private UserAccount tester;

    @Column(name = "attempt_time", nullable = false)
    private Instant attemptTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "program_status", nullable = false)
    private TestStatus programStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "go_status", nullable = false)
    private TestStatus goStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "no_go_status", nullable = false)
    private TestStatus noGoStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_status", nullable = false)
    private TestStatus resultStatus;

    @Column(name = "remark")
    private String remark;
}

