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

//    @Column(unique = true)
    private String externalId;

//    @Column(nullable = false)
    private String officialName;

//    @Column(nullable = false)
    private String normalizedName;

    private String institutionType;
    private String city;
    private String postalCode;
    private String category;

//    @Column(length = 1000)
    private String sourceUrl;
}
