package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "realized_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RealizedContract  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", unique = true)
    private Contract contract;


//    @Column(nullable = false)
    private String noticeNumber;

    @Column(columnDefinition = "TEXT")
    private String subject;

    private String contractType;
    private String procedureType;

//    @Column(precision = 18, scale = 2)
    private BigDecimal awardedValueVat;

//    @Column(precision = 18, scale = 2)
    private BigDecimal realizedValueVat;

//    @Column(precision = 18, scale = 2)
    private BigDecimal paidValueVat;

    private LocalDate publicationDate;
    private LocalDate republishDate;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;
}