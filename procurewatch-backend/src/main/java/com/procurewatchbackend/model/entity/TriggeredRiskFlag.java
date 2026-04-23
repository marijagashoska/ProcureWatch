package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "triggered_risk_flags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriggeredRiskFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    private String flagCode;

    private String flagName;

    @Column(length = 2000)
    private String flagDescription;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(length = 500)
    private String measuredValue;

    @Column(length = 500)
    private String thresholdValue;

    private LocalDateTime createdAt;
}