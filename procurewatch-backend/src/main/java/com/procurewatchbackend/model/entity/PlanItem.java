package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "procurement_plan_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private ProcurementPlan plan;

//    @Column(nullable = false, length = 2000)
    private String subject;

    private String cpvCode;
    private String contractType;
    private String procedureType;
    private String expectedStartMonth;
    private Boolean hasNotice;

//    @Column(length = 3000)
    private String notes;

//    @Column(length = 1000)
    private String sourceUrl;
}