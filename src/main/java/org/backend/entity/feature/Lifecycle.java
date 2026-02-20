package org.backend.entity.feature;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feature_lifecycle", uniqueConstraints = {
        @UniqueConstraint(name = "uk_lifecycle_member_date", columnNames = {"member_id", "feature_base_date"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lifecycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lifecycle_id")
    private Long lifecycleId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "feature_base_date")
    private LocalDate featureBaseDate;

    @Column(name = "member_lifetime_days", nullable = false)
    private Integer memberLifetimeDays;

    @Column(name = "days_since_last_activity")
    private Integer daysSinceLastActivity;

    @Column(name = "contract_end_days_left")
    private Integer contractEndDaysLeft;

    @Column(name = "is_dormant_flag", nullable = false)
    private Boolean isDormantFlag;

    @Column(name = "is_new_customer_flag", nullable = false)
    private Boolean isNewCustomerFlag;

    @Column(name = "is_terminated_flag", nullable = false)
    private Boolean isTerminatedFlag;

    @Column(name = "signup_date", nullable = false)
    private LocalDate signupDate;
}
