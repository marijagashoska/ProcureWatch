package com.procurewatchbackend.model.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "institutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institution  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String externalId;

    @Column(columnDefinition = "TEXT")
    private String officialName;

    @Column(columnDefinition = "TEXT")
    private String normalizedName;

    @Column(columnDefinition = "TEXT")
    private String institutionType;
    @Column(columnDefinition = "TEXT")
    private String city;
    @Column(columnDefinition = "TEXT")
    private String postalCode;
    @Column(columnDefinition = "TEXT")
    private String category;

    @Column(columnDefinition = "TEXT")
    private String sourceUrl;
}
