package com.procurewatchbackend.scraper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "imported_source_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportedSourceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sourceType;

    @Column(length = 2000)
    private String sourceUrl;

    private String stableKey;

    @Column(columnDefinition = "TEXT")
    private String rawFieldsJson;

    private LocalDateTime scrapedAt;

    private boolean imported;

    @Column(length = 4000)
    private String errorMessage;
}