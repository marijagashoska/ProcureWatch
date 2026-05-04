package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(mappedBy = "notice", fetch = FetchType.LAZY)
    private List<Decision> decisions;

    @Column(columnDefinition = "TEXT")
    private String noticeNumber;

    @Column(columnDefinition = "TEXT")
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String contractType;
    @Column(columnDefinition = "TEXT")
    private String procedureType;

    private LocalDate publicationDate;

    private LocalDateTime deadlineDate;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;
}