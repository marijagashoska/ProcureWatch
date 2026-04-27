package com.procurewatchbackend.service.application.impl;

import com.procurewatchbackend.dto.display.dashboard.DashboardDistributionDto;
import com.procurewatchbackend.dto.display.dashboard.DashboardKpiDto;
import com.procurewatchbackend.dto.display.dashboard.DashboardMonthlyTrendDto;
import com.procurewatchbackend.dto.display.dashboard.DashboardTopInstitutionDto;
import com.procurewatchbackend.dto.display.dashboard.DashboardTopSupplierDto;
import com.procurewatchbackend.dto.display.dashboard.GetDashboardOverviewDto;
import com.procurewatchbackend.model.entity.Contract;
import com.procurewatchbackend.model.entity.Notice;
import com.procurewatchbackend.model.entity.PlanItem;
import com.procurewatchbackend.model.entity.RiskAssessment;
import com.procurewatchbackend.model.enums.RiskLevel;
import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.NoticeRepository;
import com.procurewatchbackend.repository.PlanItemRepository;
import com.procurewatchbackend.repository.specification.ContractSpecifications;
import com.procurewatchbackend.service.application.DashboardApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardApplicationServiceImpl implements DashboardApplicationService {

    private static final String UNKNOWN_LABEL = "Unknown";

    private final ContractRepository contractRepository;
    private final NoticeRepository noticeRepository;
    private final PlanItemRepository planItemRepository;

    @Override
    public GetDashboardOverviewDto getOverview(
            LocalDate dateFrom,
            LocalDate dateTo,
            Long institutionId,
            Long supplierId,
            String procedureType,
            String contractType,
            RiskLevel riskLevel,
            int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 20));

        List<Contract> contracts = contractRepository.findAll(
                ContractSpecifications.byFilters(
                        null,
                        null,
                        institutionId,
                        supplierId,
                        contractType,
                        procedureType,
                        dateFrom,
                        dateTo,
                        null,
                        null,
                        riskLevel
                )
        );

        List<Notice> notices = noticeRepository.findAll()
                .stream()
                .filter(notice -> matchesNoticeFilters(
                        notice,
                        dateFrom,
                        dateTo,
                        institutionId,
                        procedureType,
                        contractType
                ))
                .toList();

        List<PlanItem> planItems = planItemRepository.findAll()
                .stream()
                .filter(planItem -> matchesPlanItemFilters(
                        planItem,
                        dateFrom,
                        dateTo,
                        institutionId,
                        procedureType,
                        contractType
                ))
                .toList();

        return new GetDashboardOverviewDto(
                buildKpis(contracts, notices, planItems),
                buildTopInstitutions(contracts, safeLimit),
                buildTopSuppliers(contracts, safeLimit),
                buildDistribution(contracts, "procedureType"),
                buildDistribution(contracts, "contractType"),
                buildMonthlyTrends(contracts)
        );
    }

    private DashboardKpiDto buildKpis(
            List<Contract> contracts,
            List<Notice> notices,
            List<PlanItem> planItems
    ) {
        return new DashboardKpiDto(
                contracts.size(),
                sumContractValue(contracts),
                notices.size(),
                planItems.size(),
                contracts.stream().filter(this::isHighRisk).count()
        );
    }

    private List<DashboardTopInstitutionDto> buildTopInstitutions(
            List<Contract> contracts,
            int limit
    ) {
        record InstitutionKey(Long id, String name) {
        }

        return contracts.stream()
                .filter(contract -> contract.getInstitution() != null)
                .collect(Collectors.groupingBy(contract -> new InstitutionKey(
                        contract.getInstitution().getId(),
                        contract.getInstitution().getOfficialName()
                )))
                .entrySet()
                .stream()
                .map(entry -> new DashboardTopInstitutionDto(
                        entry.getKey().id(),
                        entry.getKey().name(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue()),
                        entry.getValue().stream().filter(this::isHighRisk).count()
                ))
                .sorted(topInstitutionComparator())
                .limit(limit)
                .toList();
    }

    private List<DashboardTopSupplierDto> buildTopSuppliers(
            List<Contract> contracts,
            int limit
    ) {
        record SupplierKey(Long id, String name) {
        }

        return contracts.stream()
                .filter(contract -> contract.getSupplier() != null)
                .collect(Collectors.groupingBy(contract -> new SupplierKey(
                        contract.getSupplier().getId(),
                        contract.getSupplier().getOfficialName()
                )))
                .entrySet()
                .stream()
                .map(entry -> new DashboardTopSupplierDto(
                        entry.getKey().id(),
                        entry.getKey().name(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue()),
                        entry.getValue().stream().filter(this::isHighRisk).count()
                ))
                .sorted(topSupplierComparator())
                .limit(limit)
                .toList();
    }

    private List<DashboardDistributionDto> buildDistribution(
            List<Contract> contracts,
            String fieldName
    ) {
        Map<String, List<Contract>> grouped = contracts.stream()
                .collect(Collectors.groupingBy(contract -> switch (fieldName) {
                    case "procedureType" -> normalizeLabel(contract.getProcedureType());
                    case "contractType" -> normalizeLabel(contract.getContractType());
                    default -> UNKNOWN_LABEL;
                }));

        return grouped.entrySet()
                .stream()
                .map(entry -> new DashboardDistributionDto(
                        entry.getKey(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue())
                ))
                .sorted(Comparator
                        .comparingLong(DashboardDistributionDto::count)
                        .reversed()
                        .thenComparing(DashboardDistributionDto::label))
                .toList();
    }

    private List<DashboardMonthlyTrendDto> buildMonthlyTrends(List<Contract> contracts) {
        record MonthKey(Integer year, Integer month) {
        }

        return contracts.stream()
                .filter(contract -> resolveContractMonth(contract) != null)
                .collect(Collectors.groupingBy(contract -> {
                    YearMonth yearMonth = resolveContractMonth(contract);
                    return new MonthKey(yearMonth.getYear(), yearMonth.getMonthValue());
                }))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<Contract> contractsInMonth = entry.getValue();

                    YearMonth yearMonth = YearMonth.of(
                            entry.getKey().year(),
                            entry.getKey().month()
                    );

                    return new DashboardMonthlyTrendDto(
                            entry.getKey().year(),
                            entry.getKey().month(),
                            yearMonth.toString(),
                            contractsInMonth.size(),
                            sumContractValue(contractsInMonth),
                            contractsInMonth.stream().filter(this::isHighRisk).count()
                    );
                })
                .sorted(Comparator
                        .comparing(DashboardMonthlyTrendDto::year)
                        .thenComparing(DashboardMonthlyTrendDto::month))
                .toList();
    }

    private boolean matchesNoticeFilters(
            Notice notice,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long institutionId,
            String procedureType,
            String contractType
    ) {
        if (institutionId != null) {
            if (notice.getInstitution() == null
                    || !Objects.equals(notice.getInstitution().getId(), institutionId)) {
                return false;
            }
        }

        if (hasText(procedureType)
                && !procedureType.equalsIgnoreCase(nullToEmpty(notice.getProcedureType()))) {
            return false;
        }

        if (hasText(contractType)
                && !contractType.equalsIgnoreCase(nullToEmpty(notice.getContractType()))) {
            return false;
        }

        if (dateFrom != null) {
            if (notice.getPublicationDate() == null
                    || notice.getPublicationDate().isBefore(dateFrom)) {
                return false;
            }
        }

        if (dateTo != null) {
            return notice.getPublicationDate() != null
                    && !notice.getPublicationDate().isAfter(dateTo);
        }

        return true;
    }

    private boolean matchesPlanItemFilters(
            PlanItem planItem,
            LocalDate dateFrom,
            LocalDate dateTo,
            Long institutionId,
            String procedureType,
            String contractType
    ) {
        if (institutionId != null) {
            if (planItem.getPlan() == null
                    || planItem.getPlan().getInstitution() == null
                    || !Objects.equals(planItem.getPlan().getInstitution().getId(), institutionId)) {
                return false;
            }
        }

        if (hasText(procedureType)
                && !procedureType.equalsIgnoreCase(nullToEmpty(planItem.getProcedureType()))) {
            return false;
        }

        if (hasText(contractType)
                && !contractType.equalsIgnoreCase(nullToEmpty(planItem.getContractType()))) {
            return false;
        }

        if (dateFrom != null || dateTo != null) {
            if (planItem.getPlan() == null || planItem.getPlan().getPlanYear() == null) {
                return false;
            }

            int planYear = planItem.getPlan().getPlanYear();

            if (dateFrom != null && planYear < dateFrom.getYear()) {
                return false;
            }

            if (dateTo != null && planYear > dateTo.getYear()) {
                return false;
            }
        }

        return true;
    }

    private boolean isHighRisk(Contract contract) {
        RiskAssessment riskAssessment = contract.getRiskAssessment();

        if (riskAssessment == null || riskAssessment.getRiskLevel() == null) {
            return false;
        }

        return riskAssessment.getRiskLevel() == RiskLevel.HIGH
                || riskAssessment.getRiskLevel() == RiskLevel.CRITICAL;
    }

    private BigDecimal sumContractValue(List<Contract> contracts) {
        return contracts.stream()
                .map(Contract::getContractValueVat)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private YearMonth resolveContractMonth(Contract contract) {
        LocalDate date = contract.getContractDate() != null
                ? contract.getContractDate()
                : contract.getPublicationDate();

        return date == null ? null : YearMonth.from(date);
    }

    private String normalizeLabel(String value) {
        return hasText(value) ? value.trim() : UNKNOWN_LABEL;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private Comparator<DashboardTopInstitutionDto> topInstitutionComparator() {
        return Comparator
                .comparing(
                        DashboardTopInstitutionDto::totalContractValue,
                        Comparator.nullsLast(BigDecimal::compareTo)
                )
                .reversed()
                .thenComparing(
                        Comparator.comparingLong(DashboardTopInstitutionDto::contractCount)
                                .reversed()
                )
                .thenComparing(
                        DashboardTopInstitutionDto::institutionName,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                );
    }

    private Comparator<DashboardTopSupplierDto> topSupplierComparator() {
        return Comparator
                .comparing(
                        DashboardTopSupplierDto::totalContractValue,
                        Comparator.nullsLast(BigDecimal::compareTo)
                )
                .reversed()
                .thenComparing(
                        Comparator.comparingLong(DashboardTopSupplierDto::contractCount)
                                .reversed()
                )
                .thenComparing(
                        DashboardTopSupplierDto::supplierName,
                        Comparator.nullsLast(String::compareToIgnoreCase)
                );
    }
}