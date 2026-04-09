package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_item_id", unique = true)
    private PlanItem planItem;

    @OneToOne(mappedBy = "notice", fetch = FetchType.LAZY)
    private Decision decision;

//    @Column(nullable = false)
    private String noticeNumber;

//    @Column(nullable = false, length = 2000)
    private String subject;

    private String contractType;
    private String procedureType;
    private LocalDate publicationDate;
    private LocalDateTime deadlineDate;

//    @Column(length = 1000)
    private String sourceUrl;
}