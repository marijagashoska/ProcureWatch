package com.procurewatchbackend.model.entity;

import com.procurewatchbackend.model.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "risk_assessments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Column(precision = 10, scale = 2)
    private BigDecimal ruleScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal anomalyScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal similarityScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal clusterScore;

    @Column(precision = 10, scale = 2)
    private BigDecimal finalRiskScore;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private Integer priorityRank;

    private String modelVersion;

    private LocalDateTime evaluatedAt;

    @OneToMany(mappedBy = "riskAssessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TriggeredRiskFlag> triggeredFlags = new ArrayList<>();
}