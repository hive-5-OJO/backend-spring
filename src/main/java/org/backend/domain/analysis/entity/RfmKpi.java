package org.backend.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(name = "rfm_kpi")
@Getter
public class RfmKpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kpi_id")
    private Long id;

    private Float crr;

    @Column(name = "churn_rate")
    private Float churnRate;

    private Float nrr;

    @Column(name = "base_month", length = 7, nullable = false)
    private String baseMonth;
}
