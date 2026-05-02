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
@Table(name = "contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @OneToOne // Секој договор има еден поврзан оглас
    @JoinColumn(name = "notice_id")
    private Notice notice;

    // Додади Getter и Setter (или користи @Data од Lombok)
    public Notice getNotice() {
        return notice;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "contract")
    private RealizedContract realizedContract;

    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY)
    private Decision decision;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @OneToOne(mappedBy = "contract", fetch = FetchType.LAZY)
    private RiskAssessment riskAssessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

//    @Column(nullable = false)
    private String noticeNumber;

//    @Column(nullable = false, length = 2000)
    private String subject;

    private String contractType;
    private String procedureType;
    private LocalDate contractDate;
    private LocalDate publicationDate;

//    @Column(precision = 18, scale = 2)
    private BigDecimal estimatedValueVat;

//    @Column(precision = 18, scale = 2)
    private BigDecimal contractValueVat;

    private String currency;

//    @Column(length = 1000)
    private String sourceUrl;

}
