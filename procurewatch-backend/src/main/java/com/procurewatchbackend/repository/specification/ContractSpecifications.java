package com.procurewatchbackend.repository.specification;

import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Institution;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.entity.Supplier;
import com.procurewatchbackend.model.enums.RiskLevel;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ContractSpecifications {

    private ContractSpecifications() {
    }

    public static Specification<Contract> byFilters(
            String searchText,
            String noticeNumber,
            Long institutionId,
            Long supplierId,
            String contractType,
            String procedureType,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal minValue,
            BigDecimal maxValue,
            RiskLevel riskLevel
    ) {
        return (root, query, cb) -> {
            query.distinct(true);

            Join<Contract, Institution> institutionJoin = root.join("institution", JoinType.INNER);
            Join<Contract, Supplier> supplierJoin = root.join("supplier", JoinType.LEFT);
            Join<Contract, RiskAssessment> riskJoin = root.join("riskAssessment", JoinType.LEFT);

            List<Predicate> predicates = new ArrayList<>();

            if (hasText(searchText)) {
                String pattern = "%" + searchText.trim().toLowerCase() + "%";

                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("subject")), pattern),
                        cb.like(cb.lower(root.get("noticeNumber")), pattern),
                        cb.like(cb.lower(institutionJoin.get("officialName")), pattern),
                        cb.like(cb.lower(supplierJoin.get("officialName")), pattern)
                ));
            }

            if (hasText(noticeNumber)) {
                predicates.add(
                        cb.equal(cb.lower(root.get("noticeNumber")), noticeNumber.trim().toLowerCase())
                );
            }

            if (institutionId != null) {
                predicates.add(cb.equal(institutionJoin.get("id"), institutionId));
            }

            if (supplierId != null) {
                predicates.add(cb.equal(supplierJoin.get("id"), supplierId));
            }

            if (hasText(contractType)) {
                predicates.add(
                        cb.equal(cb.lower(root.get("contractType")), contractType.trim().toLowerCase())
                );
            }

            if (hasText(procedureType)) {
                predicates.add(
                        cb.equal(cb.lower(root.get("procedureType")), procedureType.trim().toLowerCase())
                );
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("contractDate"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("contractDate"), dateTo));
            }

            if (minValue != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("contractValueVat"), minValue));
            }

            if (maxValue != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("contractValueVat"), maxValue));
            }

            if (riskLevel != null) {
                predicates.add(cb.equal(riskJoin.get("riskLevel"), riskLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}