package com.procurewatchbackend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Decision> decisions;

//    @Column(unique = true)
    private String externalId;

//    @Column(nullable = false)
    private String officialName;

//    @Column(nullable = false)
    private String normalizedName;

//    @Column(length = 3000)
    private String realOwnersInfo;

//    @Column(length = 1000)
    private String sourceUrl;
}
