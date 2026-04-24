package com.procurewatchbackend.service.importer;

import com.procurewatchbackend.dto.importer.ImportResultDto;
import com.procurewatchbackend.model.entity.*;
import com.procurewatchbackend.repository.*;
import com.procurewatchbackend.scraper.ENabavkiBrowserClient;
import com.procurewatchbackend.scraper.ScrapedRow;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import com.procurewatchbackend.util.TextNormalizer;
import com.procurewatchbackend.util.ValueParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ENabavkiFullImportService {

    private static final String ANNUAL_PLANS_ROUTE = "/annual-plans";
    private static final String NOTICES_ROUTE = "/notices";
    private static final String DECISIONS_ROUTE = "/tender-winners/0";
    private static final String CONTRACTS_ROUTE = "/contracts/0";
    private static final String REALIZED_CONTRACTS_ROUTE = "/realized-contract";

    private final ENabavkiBrowserClient browserClient;
    private final InstitutionRepository institutionRepository;
    private final SupplierRepository supplierRepository;
    private final ProcurementPlanRepository procurementPlanRepository;
    private final PlanItemRepository planItemRepository;
    private final NoticeRepository noticeRepository;
    private final DecisionRepository decisionRepository;
    private final ContractRepository contractRepository;
    private final RealizedContractRepository realizedContractRepository;
    private final RiskAssessmentDomainService riskAssessmentDomainService;

    @Transactional
    public ImportResultDto importAll(
            Integer fromYear,
            Integer toYear,
            int maxPages,
            int maxAnnualPlanDetails,
            boolean evaluateRisk
    ) {
        ImportResultDto result = ImportResultDto.builder().build();

        List<ScrapedRow> annualPlanRows = browserClient.scrapeList(ANNUAL_PLANS_ROUTE, maxPages);
        importProcurementPlans(annualPlanRows, fromYear, toYear, result);

        List<ScrapedRow> planItemRows =
                browserClient.scrapeAnnualPlanItemDetails(annualPlanRows, maxAnnualPlanDetails);
        importPlanItems(planItemRows, fromYear, toYear, result);

        importNotices(browserClient.scrapeList(NOTICES_ROUTE, maxPages), fromYear, toYear, result);
        importDecisions(browserClient.scrapeList(DECISIONS_ROUTE, maxPages), fromYear, toYear, result);
        importContracts(browserClient.scrapeList(CONTRACTS_ROUTE, maxPages), fromYear, toYear, result);
        importRealizedContracts(browserClient.scrapeList(REALIZED_CONTRACTS_ROUTE, maxPages), fromYear, toYear, result);

        if (evaluateRisk) {
            result.setRiskAssessmentsGenerated(riskAssessmentDomainService.evaluateAllContracts().size());
        }

        return result;
    }

    private void importProcurementPlans(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String institutionName = institutionName(row);

            if (!hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            LocalDate publicationDate = publicationDate(row);

            Integer year = firstNonNull(
                    ValueParser.parseYear(row.get("Година", "План за година", "year")),
                    publicationDate == null ? null : publicationDate.getYear()
            );

            if (!yearAllowed(year, fromYear, toYear)) {
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);

            Optional<ProcurementPlan> existing =
                    procurementPlanRepository.findFirstByInstitutionIdAndPlanYear(institution.getId(), year);

            if (existing.isEmpty()) {
                ProcurementPlan plan = ProcurementPlan.builder()
                        .institution(institution)
                        .planYear(year)
                        .publicationDate(publicationDate)
                        .sourceUrl(row.sourceUrl())
                        .build();

                procurementPlanRepository.save(plan);
                result.setProcurementPlansImported(result.getProcurementPlansImported() + 1);
            }
        }
    }

    private void importPlanItems(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String institutionName = firstText(row.get("_parentInstitution"), institutionName(row));
            String subject = subject(row);

            if (!hasText(institutionName) || !hasText(subject)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            LocalDate publicationDate = firstNonNull(
                    publicationDate(row),
                    ValueParser.parseDate(row.get("_parentPublicationDate"))
            );

            Integer year = firstNonNull(
                    ValueParser.parseYear(row.get("_parentYear")),
                    ValueParser.parseYear(row.get("Година", "year")),
                    publicationDate == null ? null : publicationDate.getYear()
            );

            if (!yearAllowed(year, fromYear, toYear)) {
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);

            ProcurementPlan plan = procurementPlanRepository
                    .findFirstByInstitutionIdAndPlanYear(institution.getId(), year)
                    .orElseGet(() -> procurementPlanRepository.save(
                            ProcurementPlan.builder()
                                    .institution(institution)
                                    .planYear(year)
                                    .publicationDate(publicationDate)
                                    .sourceUrl(firstText(row.get("_parentSourceUrl"), row.sourceUrl()))
                                    .build()
                    ));

            String cpvCode = row.get("ЗПЈН", "CPV", "cpvCode");

            Optional<PlanItem> existing =
                    planItemRepository.findFirstByPlanIdAndSubjectIgnoreCaseAndCpvCode(
                            plan.getId(),
                            subject,
                            cpvCode
                    );

            PlanItem item = existing.orElseGet(() -> PlanItem.builder()
                    .plan(plan)
                    .subject(subject)
                    .cpvCode(cpvCode)
                    .build());

            item.setContractType(contractType(row));
            item.setProcedureType(procedureType(row));
            item.setExpectedStartMonth(row.get("Очекуван старт", "expectedStartMonth"));
            item.setHasNotice(parseBoolean(row.get("Оглас", "hasNotice")));
            item.setNotes(row.get("Забелешки", "notes"));
            item.setSourceUrl(firstText(row.sourceUrl(), row.get("_parentSourceUrl")));

            planItemRepository.save(item);

            if (existing.isEmpty()) {
                result.setPlanItemsImported(result.getPlanItemsImported() + 1);
            }
        }
    }

    private void importNotices(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String noticeNumber = noticeNumber(row);
            String institutionName = institutionName(row);
            String subject = subject(row);
            LocalDate publicationDate = publicationDate(row);

            if (!yearAllowed(publicationDate == null ? null : publicationDate.getYear(), fromYear, toYear)) {
                continue;
            }

            if (!hasText(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);

            Optional<Notice> existing = noticeRepository.findFirstByNoticeNumber(noticeNumber);

            Notice notice = existing.orElseGet(() -> Notice.builder()
                    .noticeNumber(noticeNumber)
                    .institution(institution)
                    .build());

            notice.setInstitution(institution);
            notice.setSubject(subject);
            notice.setContractType(contractType(row));
            notice.setProcedureType(procedureType(row));
            notice.setPublicationDate(publicationDate);
            notice.setDeadlineDate(deadlineDate(row));
            notice.setSourceUrl(row.sourceUrl());

            if (notice.getPlanItem() == null && publicationDate != null && hasText(subject)) {
                findPlanItem(institution.getId(), subject, publicationDate.getYear())
                        .ifPresent(planItem -> {
                            notice.setPlanItem(planItem);
                            planItem.setHasNotice(true);
                            planItemRepository.save(planItem);
                        });
            }

            noticeRepository.save(notice);

            if (existing.isEmpty()) {
                result.setNoticesImported(result.getNoticesImported() + 1);
            }
        }
    }

    private void importDecisions(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String noticeNumber = noticeNumber(row);
            String institutionName = institutionName(row);
            String supplierName = supplierName(row);
            LocalDate decisionDate = decisionDate(row);
            String subject = subject(row);

            if (!yearAllowed(decisionDate == null ? null : decisionDate.getYear(), fromYear, toYear)) {
                continue;
            }

            if (!hasText(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);
            Supplier supplier = hasText(supplierName) ? getOrCreateSupplier(row, supplierName, result) : null;
            Notice notice = getOrCreateNoticePlaceholder(noticeNumber, institution, subject, row.sourceUrl());

            Optional<Decision> existing = supplier == null
                    ? decisionRepository.findFirstByNoticeNumber(noticeNumber)
                    : decisionRepository.findFirstByNoticeNumberAndSupplierIdAndDecisionDate(
                    noticeNumber,
                    supplier.getId(),
                    decisionDate
            );

            Decision decision = existing.orElseGet(() -> Decision.builder()
                    .notice(notice)
                    .institution(institution)
                    .supplier(supplier)
                    .noticeNumber(noticeNumber)
                    .build());

            decision.setNotice(notice);
            decision.setInstitution(institution);
            decision.setSupplier(supplier);
            decision.setDecisionDate(decisionDate);
            decision.setSubject(subject);
            decision.setDecisionText(firstText(row.get("Текст на одлука", "decisionText"), subject));
            decision.setProcedureType(procedureType(row));
            decision.setSourceUrl(row.sourceUrl());

            findContract(noticeNumber, institution.getId(), supplier == null ? null : supplier.getId(), subject)
                    .ifPresent(decision::setContract);

            decisionRepository.save(decision);

            if (existing.isEmpty()) {
                result.setDecisionsImported(result.getDecisionsImported() + 1);
            }
        }
    }

    private void importContracts(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String noticeNumber = noticeNumber(row);
            String institutionName = institutionName(row);
            String supplierName = supplierName(row);
            String subject = subject(row);
            LocalDate contractDate = contractDate(row);
            LocalDate publicationDate = publicationDate(row);

            Integer year = firstNonNull(
                    contractDate == null ? null : contractDate.getYear(),
                    publicationDate == null ? null : publicationDate.getYear()
            );

            if (!yearAllowed(year, fromYear, toYear)) {
                continue;
            }

            if (!hasText(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);
            Supplier supplier = hasText(supplierName) ? getOrCreateSupplier(row, supplierName, result) : null;

            Optional<Contract> existing =
                    findContract(noticeNumber, institution.getId(), supplier == null ? null : supplier.getId(), subject);

            Contract contract = existing.orElseGet(() -> Contract.builder()
                    .noticeNumber(noticeNumber)
                    .institution(institution)
                    .supplier(supplier)
                    .build());

            contract.setInstitution(institution);
            contract.setSupplier(supplier);
            contract.setSubject(subject);
            contract.setContractType(contractType(row));
            contract.setProcedureType(procedureType(row));
            contract.setContractDate(contractDate);
            contract.setPublicationDate(publicationDate);
            contract.setEstimatedValueVat(estimatedValue(row));
            contract.setContractValueVat(contractValue(row));
            contract.setCurrency(ValueParser.detectCurrency(firstText(row.get("Валута", "currency"), row.fields().toString())));
            contract.setSourceUrl(row.sourceUrl());

            Contract saved = contractRepository.save(contract);

            if (existing.isEmpty()) {
                result.setContractsImported(result.getContractsImported() + 1);
            }

            decisionRepository.findFirstByNoticeNumber(noticeNumber).ifPresent(decision -> {
                if (decision.getContract() == null) {
                    decision.setContract(saved);
                    decisionRepository.save(decision);
                }
            });
        }
    }

    private void importRealizedContracts(
            List<ScrapedRow> rows,
            Integer fromYear,
            Integer toYear,
            ImportResultDto result
    ) {
        for (ScrapedRow row : rows) {
            String noticeNumber = noticeNumber(row);
            String institutionName = institutionName(row);
            String supplierName = supplierName(row);
            String subject = subject(row);
            LocalDate publicationDate = publicationDate(row);

            if (!yearAllowed(publicationDate == null ? null : publicationDate.getYear(), fromYear, toYear)) {
                continue;
            }

            if (!hasText(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);
            Supplier supplier = hasText(supplierName) ? getOrCreateSupplier(row, supplierName, result) : null;

            Contract contract =
                    findContract(noticeNumber, institution.getId(), supplier == null ? null : supplier.getId(), subject)
                            .orElse(null);

            Optional<RealizedContract> existing = contract != null
                    ? realizedContractRepository.findByContractId(contract.getId())
                    : realizedContractRepository.findFirstByNoticeNumberAndInstitutionIdAndSupplierId(
                    noticeNumber,
                    institution.getId(),
                    supplier == null ? null : supplier.getId()
            );

            RealizedContract realizedContract = existing.orElseGet(() -> RealizedContract.builder()
                    .noticeNumber(noticeNumber)
                    .institution(institution)
                    .supplier(supplier)
                    .contract(contract)
                    .build());

            realizedContract.setInstitution(institution);
            realizedContract.setSupplier(supplier);
            realizedContract.setContract(contract);
            realizedContract.setSubject(subject);
            realizedContract.setContractType(contractType(row));
            realizedContract.setProcedureType(procedureType(row));
            realizedContract.setAwardedValueVat(awardedValue(row));
            realizedContract.setRealizedValueVat(realizedValue(row));
            realizedContract.setPaidValueVat(paidValue(row));
            realizedContract.setPublicationDate(publicationDate);
            realizedContract.setRepublishDate(republishDate(row));
            realizedContract.setSourceUrl(row.sourceUrl());

            realizedContractRepository.save(realizedContract);

            if (existing.isEmpty()) {
                result.setRealizedContractsImported(result.getRealizedContractsImported() + 1);
            }
        }
    }

    private Institution getOrCreateInstitution(ScrapedRow row, String officialName, ImportResultDto result) {
        String normalized = TextNormalizer.normalizeName(officialName);

        return institutionRepository.findFirstByNormalizedName(normalized)
                .orElseGet(() -> {
                    Institution institution = Institution.builder()
                            .externalId(row.get("externalId", "ID", "Шифра"))
                            .officialName(TextNormalizer.safe(officialName))
                            .normalizedName(normalized)
                            .institutionType(row.get("Тип на институција", "institutionType"))
                            .city(row.get("Град", "city"))
                            .postalCode(row.get("Поштенски број", "postalCode"))
                            .category(row.get("Категорија", "category"))
                            .sourceUrl(row.sourceUrl())
                            .build();

                    result.setInstitutionsImported(result.getInstitutionsImported() + 1);
                    return institutionRepository.save(institution);
                });
    }

    private Supplier getOrCreateSupplier(ScrapedRow row, String officialName, ImportResultDto result) {
        String normalized = TextNormalizer.normalizeName(officialName);

        return supplierRepository.findFirstByNormalizedName(normalized)
                .orElseGet(() -> {
                    Supplier supplier = Supplier.builder()
                            .externalId(row.get("externalId", "ID", "Шифра"))
                            .officialName(TextNormalizer.safe(officialName))
                            .normalizedName(normalized)
                            .realOwnersInfo(realOwners(row))
                            .sourceUrl(row.sourceUrl())
                            .build();

                    result.setSuppliersImported(result.getSuppliersImported() + 1);
                    return supplierRepository.save(supplier);
                });
    }

    private Notice getOrCreateNoticePlaceholder(
            String noticeNumber,
            Institution institution,
            String subject,
            String sourceUrl
    ) {
        return noticeRepository.findFirstByNoticeNumber(noticeNumber)
                .orElseGet(() -> noticeRepository.save(
                        Notice.builder()
                                .noticeNumber(noticeNumber)
                                .institution(institution)
                                .subject(subject)
                                .sourceUrl(sourceUrl)
                                .build()
                ));
    }

    private Optional<Contract> findContract(
            String noticeNumber,
            Long institutionId,
            Long supplierId,
            String subject
    ) {
        if (hasText(noticeNumber) && institutionId != null && supplierId != null && hasText(subject)) {
            Optional<Contract> found =
                    contractRepository.findFirstByNoticeNumberAndInstitutionIdAndSupplierIdAndSubjectContainingIgnoreCase(
                            noticeNumber,
                            institutionId,
                            supplierId,
                            TextNormalizer.firstPart(subject, 80)
                    );

            if (found.isPresent()) {
                return found;
            }
        }

        if (hasText(noticeNumber)) {
            return contractRepository.findFirstByNoticeNumber(noticeNumber);
        }

        return Optional.empty();
    }

    private Optional<PlanItem> findPlanItem(Long institutionId, String subject, Integer year) {
        if (institutionId == null || !hasText(subject) || year == null) {
            return Optional.empty();
        }

        return planItemRepository.findFirstByPlan_Institution_IdAndSubjectContainingIgnoreCaseAndPlan_PlanYear(
                institutionId,
                TextNormalizer.firstPart(subject, 80),
                year
        );
    }

    private boolean yearAllowed(Integer rowYear, Integer fromYear, Integer toYear) {
        if (rowYear == null) {
            return true;
        }

        if (fromYear != null && rowYear < fromYear) {
            return false;
        }

        return toYear == null || rowYear <= toYear;
    }

    private String institutionName(ScrapedRow row) {
        return row.get(
                "Назив на договорниот орган",
                "Договорен орган",
                "Институција",
                "Contracting authority",
                "Institution",
                "institution"
        );
    }

    private String supplierName(ScrapedRow row) {
        return row.get(
                "Носител на набавка",
                "Добавувач",
                "Економски оператор",
                "Избран понудувач",
                "Оператор",
                "Supplier",
                "supplier"
        );
    }

    private String noticeNumber(ScrapedRow row) {
        return row.get(
                "Број на оглас",
                "Оглас",
                "Број на постапка",
                "Број",
                "noticeNumber",
                "brojNaOglas"
        );
    }

    private String subject(ScrapedRow row) {
        return row.get(
                "Предмет на договорот за јавна набавка",
                "Предмет на набавка",
                "Предмет",
                "Опис",
                "subject"
        );
    }

    private String contractType(ScrapedRow row) {
        return row.get("Вид на договор за јавна набавка", "Вид на договор", "contractType");
    }

    private String procedureType(ScrapedRow row) {
        return row.get("Вид на постапка", "Постапка", "procedureType");
    }

    private String realOwners(ScrapedRow row) {
        return row.get("Вистински сопственици", "Податоци за сопственици", "realOwnersInfo");
    }

    private LocalDate publicationDate(ScrapedRow row) {
        return ValueParser.parseDate(row.get("Датум на објава", "Датум на објавување", "publicationDate"));
    }

    private LocalDateTime deadlineDate(ScrapedRow row) {
        return ValueParser.parseDateTime(row.get("Краен рок", "Рок за поднесување", "deadlineDate"));
    }

    private LocalDate decisionDate(ScrapedRow row) {
        return ValueParser.parseDate(row.get("Датум на одлука", "Датум на избор", "decisionDate"));
    }

    private LocalDate contractDate(ScrapedRow row) {
        return ValueParser.parseDate(row.get("Датум на договор", "Датум на склучување на договор", "contractDate"));
    }

    private LocalDate republishDate(ScrapedRow row) {
        return ValueParser.parseDate(row.get("Датум на повторна објава", "republishDate"));
    }

    private BigDecimal estimatedValue(ScrapedRow row) {
        return ValueParser.parseMoney(row.get("Проценета вредност", "estimatedValueVat"));
    }

    private BigDecimal contractValue(ScrapedRow row) {
        return ValueParser.parseMoney(row.get("Вредност на договор", "Вкупна вредност со ДДВ", "contractValueVat"));
    }

    private BigDecimal awardedValue(ScrapedRow row) {
        return ValueParser.parseMoney(row.get("Доделена вредност", "awardedValueVat"));
    }

    private BigDecimal realizedValue(ScrapedRow row) {
        return ValueParser.parseMoney(row.get("Реализирана вредност", "realizedValueVat"));
    }

    private BigDecimal paidValue(ScrapedRow row) {
        return ValueParser.parseMoney(row.get("Платена вредност", "paidValueVat"));
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }

        String normalized = TextNormalizer.normalizeKey(value);
        return normalized.contains("да")
                || normalized.contains("yes")
                || normalized.contains("true");
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }

        for (T value : values) {
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private static String firstText(String... values) {
        if (values == null) {
            return null;
        }

        for (String value : values) {
            if (hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}