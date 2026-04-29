package com.procurewatchbackend.service.importer;

import com.procurewatchbackend.dto.importer.ImportResultDto;
import com.procurewatchbackend.model.entity.*;
import com.procurewatchbackend.repository.*;
import com.procurewatchbackend.scraper.ENabavkiBrowserClient;
import com.procurewatchbackend.scraper.MojibakeFixer;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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

        int maxDetails = maxAnnualPlanDetails;

        importNotices(
                browserClient.scrapeListWithDetails(NOTICES_ROUTE, maxPages, maxDetails),
                fromYear,
                toYear,
                result
        );

        importDecisions(
                browserClient.scrapeListWithDetails(DECISIONS_ROUTE, maxPages, maxDetails),
                fromYear,
                toYear,
                result
        );

        importContracts(
                browserClient.scrapeList(CONTRACTS_ROUTE, maxPages),
                fromYear,
                toYear,
                result
        );

        importRealizedContracts(
                browserClient.scrapeListWithDetails(REALIZED_CONTRACTS_ROUTE, maxPages, maxDetails),
                fromYear,
                toYear,
                result
        );

        linkProcurementLifecycle();

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
                    ValueParser.parseYear(cleanScrapedText(row.get("Година", "План за година", "year"))),
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
            String institutionName = firstText(
                    cleanScrapedText(row.get("_institutionOfficialName")),
                    cleanScrapedText(row.get("_parentInstitution")),
                    institutionName(row)
            );

            String subject = subject(row);

            if (!hasText(institutionName) || !hasText(subject)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            LocalDate publicationDate = firstNonNull(
                    publicationDate(row),
                    ValueParser.parseDate(cleanScrapedText(row.get("_parentPublicationDate")))
            );

            Integer year = firstNonNull(
                    ValueParser.parseYear(cleanScrapedText(row.get("_parentYear"))),
                    ValueParser.parseYear(cleanScrapedText(row.get("Година", "year"))),
                    publicationDate == null ? null : publicationDate.getYear()
            );

            if (!yearAllowed(year, fromYear, toYear)) {
                continue;
            }

            /*
             * IMPORTANT:
             * This row now contains:
             * _institutionOfficialName
             * _institutionCity
             * _institutionPostalCode
             * _institutionCategory
             *
             * So this call enriches Institution before creating/finding the plan.
             */
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

            String cpvCode = cleanScrapedText(row.get(
                    "ЗПЈН",
                    "ЗЈН",
                    "CPV",
                    "CPV код",
                    "cpvCode"
            ));

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
            item.setExpectedStartMonth(cleanScrapedText(row.get(
                    "Очекуван почеток",
                    "Очекуван старт",
                    "Месец",
                    "expectedStartMonth"
            )));
            item.setHasNotice(parseBoolean(row.get(
                    "_hasNotice",
                    "Оглас",
                    "Има оглас",
                    "Дали има оглас",
                    "hasNotice"
            )));
            item.setNotes(cleanScrapedText(row.get("Забелешки", "notes")));
            item.setSourceUrl(firstText(row.sourceUrl(), row.get("_parentSourceUrl")));

            planItemRepository.save(item);

            importNoticeLinkedToPlanItemIfPresent(
                    row,
                    institution,
                    item,
                    publicationDate,
                    result
            );


            if (existing.isEmpty()) {
                result.setPlanItemsImported(result.getPlanItemsImported() + 1);
            }
        }
    }

    private void importNoticeLinkedToPlanItemIfPresent(
            ScrapedRow row,
            Institution fallbackInstitution,
            PlanItem planItem,
            LocalDate fallbackPublicationDate,
            ImportResultDto result
    ) {
        ScrapedRow noticeRow = noticeOnlyRow(row);
        String noticeNumber = noticeNumber(noticeRow);

        if (!hasText(noticeNumber)) {
            return;
        }

        Optional<Notice> existing = noticeRepository.findFirstByNoticeNumber(noticeNumber);

        if (existing.isPresent()) {
            Notice alreadySaved = existing.get();

            if (alreadySaved.getPlanItem() == null && planItem != null) {
                alreadySaved.setPlanItem(planItem);
                noticeRepository.save(alreadySaved);
            }

            if (planItem != null && !Boolean.TRUE.equals(planItem.getHasNotice())) {
                planItem.setHasNotice(true);
                planItemRepository.save(planItem);
            }

            return;
        }

        String noticeInstitutionName = firstText(
                institutionName(noticeRow),
                fallbackInstitution == null ? null : fallbackInstitution.getOfficialName()
        );

        if (!hasText(noticeInstitutionName)) {
            result.setSkippedRows(result.getSkippedRows() + 1);
            return;
        }

        Institution institution = getOrCreateInstitution(noticeRow, noticeInstitutionName, result);

        Notice notice = Notice.builder()
                .noticeNumber(noticeNumber)
                .institution(institution)
                .planItem(planItem)
                .build();

        notice.setSubject(firstText(subject(noticeRow), planItem == null ? null : planItem.getSubject()));
        notice.setContractType(firstText(contractType(noticeRow), planItem == null ? null : planItem.getContractType()));
        notice.setProcedureType(firstText(procedureType(noticeRow), planItem == null ? null : planItem.getProcedureType()));
        notice.setPublicationDate(firstNonNull(publicationDate(noticeRow), fallbackPublicationDate));
        notice.setDeadlineDate(deadlineDate(noticeRow));
        notice.setSourceUrl(noticeRow.sourceUrl());

        noticeRepository.save(notice);

        if (planItem != null && !Boolean.TRUE.equals(planItem.getHasNotice())) {
            planItem.setHasNotice(true);
            planItemRepository.save(planItem);
        }

        result.setNoticesImported(result.getNoticesImported() + 1);
    }

    private ScrapedRow noticeOnlyRow(ScrapedRow row) {
        Map<String, String> fields = new LinkedHashMap<>();
        String prefix = "_notice_";

        for (Map.Entry<String, String> entry : row.fields().entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key == null || value == null || !key.startsWith(prefix)) {
                continue;
            }

            String unprefixedKey = key.substring(prefix.length());

            if (!unprefixedKey.isBlank() && !value.isBlank()) {
                fields.put(unprefixedKey, value);
            }
        }

        putIfBlank(fields, "_institutionOfficialName", row.get("_institutionOfficialName"));
        putIfBlank(fields, "_parentInstitution", row.get("_parentInstitution"));
        putIfBlank(fields, "Назив на договорниот орган", row.get("Назив на договорниот орган"));
        putIfBlank(fields, "Предмет на набавка", subject(row));
        putIfBlank(fields, "Вид на договор за јавна набавка", contractType(row));
        putIfBlank(fields, "Вид на постапка", procedureType(row));

        return new ScrapedRow(
                fields,
                firstText(row.get("_noticeSourceUrl"), row.get("_noticeUrl"), row.sourceUrl())
        );
    }

    private String decisionText(ScrapedRow row) {
        return cleanScrapedText(firstText(
                row.get(
                        "Одлуки",
                        "Одлука",
                        "Текст на одлука",
                        "decisionText"
                ),
                subject(row)
        ));
    }

    private String supplierNameFromDecisionText(String decisionText) {
        decisionText = cleanScrapedText(decisionText);

        if (!hasText(decisionText)) {
            return null;
        }

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(?iu)понуда\\.\\s*(.+?)\\s+е\\s+избран"
        );

        java.util.regex.Matcher matcher = pattern.matcher(decisionText);

        if (matcher.find()) {
            return cleanScrapedText(matcher.group(1));
        }

        return null;
    }

    private boolean decisionLooksLikeAward(Decision decision) {
        String text = cleanScrapedText(firstText(
                decision.getDecisionText(),
                decision.getSubject()
        ));

        if (!hasText(text)) {
            return false;
        }

        String lower = text.toLowerCase();

        if (lower.contains("поништување")) {
            return false;
        }

        return lower.contains("избор на најповолна")
                || lower.contains("избран за најповолен")
                || lower.contains("избран за најповолна")
                || lower.contains("најповолен понудувач");
    }

    private boolean decisionMatchesSupplier(Decision decision, Supplier supplier) {
        if (supplier == null || !hasText(supplier.getOfficialName())) {
            return true;
        }

        String supplierName = TextNormalizer.normalizeName(supplier.getOfficialName());

        if (decision.getSupplier() != null && hasText(decision.getSupplier().getOfficialName())) {
            String decisionSupplier = TextNormalizer.normalizeName(decision.getSupplier().getOfficialName());

            if (decisionSupplier.equals(supplierName)) {
                return true;
            }
        }

        String decisionText = TextNormalizer.normalizeName(firstText(
                decision.getDecisionText(),
                decision.getSubject()
        ));

        return hasText(decisionText) && decisionText.contains(supplierName);
    }

    private Optional<Decision> findMatchingDecisionForContract(Contract contract) {
        String noticeNumber = normalizeNoticeNumber(contract.getNoticeNumber());
        Supplier supplier = contract.getSupplier();

        if (!hasText(noticeNumber) || supplier == null || supplier.getId() == null) {
            return Optional.empty();
        }

        Optional<Decision> exactSupplierMatch =
                decisionRepository.findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
                        noticeNumber,
                        supplier.getId()
                );

        if (exactSupplierMatch.isPresent()) {
            return exactSupplierMatch;
        }

        return decisionRepository.findByNoticeNumberIgnoreCaseOrderByIdAsc(noticeNumber)
                .stream()
                .filter(decision -> decisionMatchesSupplier(decision, supplier))
                .findFirst();
    }

    private Optional<Contract> findMatchingContractForDecision(Decision decision) {
        String noticeNumber = normalizeNoticeNumber(decision.getNoticeNumber());
        Supplier supplier = decision.getSupplier();

        if (!hasText(noticeNumber) || supplier == null || supplier.getId() == null) {
            return Optional.empty();
        }

        return contractRepository.findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
                noticeNumber,
                supplier.getId()
        );
    }

    private void syncContractDecisionLink(Contract contract) {
        if (contract == null || contract.getId() == null) {
            return;
        }

        Optional<Decision> matchingDecision = findMatchingDecisionForContract(contract);
        Long matchingDecisionId = matchingDecision.map(Decision::getId).orElse(null);

        for (Decision linkedDecision : decisionRepository.findByContractId(contract.getId())) {
            if (!java.util.Objects.equals(linkedDecision.getId(), matchingDecisionId)) {
                linkedDecision.setContract(null);
                decisionRepository.save(linkedDecision);
            }
        }

        matchingDecision.ifPresent(decision -> {
            Contract alreadyLinkedContract = decision.getContract();

            if (alreadyLinkedContract == null || !java.util.Objects.equals(alreadyLinkedContract.getId(), contract.getId())) {
                decision.setContract(contract);
                decisionRepository.save(decision);
            }
        });
    }

    private void putIfBlank(Map<String, String> fields, String key, String value) {
        if (!hasText(key) || !hasText(value)) {
            return;
        }

        String existing = fields.get(key);

        if (!hasText(existing)) {
            fields.put(key, value.trim());
        }
    }

    private boolean isValidNoticeNumber(String noticeNumber) {
        if (!hasText(noticeNumber)) {
            return false;
        }

        String value = noticeNumber.trim();

        if (!value.matches(".*\\d.*")) {
            return false;
        }

        if (value.equalsIgnoreCase("Договорен орган")
                || value.equalsIgnoreCase("Институција")
                || value.equalsIgnoreCase("Одлуки")
                || value.equalsIgnoreCase("Број")
                || value.equalsIgnoreCase("Предмет")
                || value.equalsIgnoreCase("Постапка")) {
            return false;
        }

        return value.length() <= 100;
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

            if (!isValidNoticeNumber(noticeNumber) || !hasText(institutionName)) {
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
    private Notice findNoticeOrNull(String noticeNumber) {
        if (!hasText(noticeNumber)) {
            return null;
        }

        return noticeRepository.findFirstByNoticeNumber(noticeNumber)
                .orElse(null);
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
            LocalDate decisionDate = decisionDate(row);
            String subject = subject(row);
            String decisionText = decisionText(row);

            String supplierName = firstText(
                    supplierName(row),
                    supplierNameFromDecisionText(decisionText)
            );

            if (!yearAllowed(decisionDate == null ? null : decisionDate.getYear(), fromYear, toYear)) {
                continue;
            }

            if (!isValidNoticeNumber(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);
            Supplier supplier = hasText(supplierName) ? getOrCreateSupplier(row, supplierName, result) : null;

            Notice notice = findNoticeOrNull(noticeNumber);

            Optional<Decision> existing = Optional.empty();

            if (hasText(row.sourceUrl())) {
                existing = decisionRepository.findFirstBySourceUrl(row.sourceUrl());
            }

            if (existing.isEmpty() && supplier != null) {
                existing = decisionRepository.findFirstByNoticeNumberAndSupplierIdAndDecisionDate(
                        noticeNumber,
                        supplier.getId(),
                        decisionDate
                );
            }

            if (existing.isEmpty() && hasText(subject) && hasText(decisionText)) {
                existing = decisionRepository.findFirstByNoticeNumberAndSubjectContainingIgnoreCaseAndDecisionTextContainingIgnoreCase(
                        noticeNumber,
                        TextNormalizer.firstPart(subject, 80),
                        TextNormalizer.firstPart(decisionText, 80)
                );
            }

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
            decision.setDecisionText(decisionText);
            decision.setProcedureType(procedureType(row));
            decision.setSourceUrl(row.sourceUrl());

            decision.setContract(findMatchingContractForDecision(decision).orElse(null));

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

            if (!isValidNoticeNumber(noticeNumber) || !hasText(institutionName)) {
                result.setSkippedRows(result.getSkippedRows() + 1);
                continue;
            }

            Institution institution = getOrCreateInstitution(row, institutionName, result);
            Supplier supplier = hasText(supplierName) ? getOrCreateSupplier(row, supplierName, result) : null;

            getOrCreateNoticePlaceholder(
                    noticeNumber,
                    institution,
                    subject,
                    row.sourceUrl()
            );

            Optional<Contract> existing =
                    findContractForContractImport(
                            row.sourceUrl(),
                            noticeNumber,
                            institution.getId(),
                            supplier == null ? null : supplier.getId(),
                            subject
                    );

            Contract contract = existing.orElseGet(() -> Contract.builder()
                    .noticeNumber(noticeNumber)
                    .institution(institution)
                    .supplier(supplier)
                    .build());

            contract.setNoticeNumber(noticeNumber);
            contract.setInstitution(institution);
            contract.setSupplier(supplier);
            contract.setSubject(subject);
            contract.setContractType(contractType(row));
            contract.setProcedureType(procedureType(row));
            contract.setContractDate(contractDate);
            contract.setPublicationDate(publicationDate);
            contract.setEstimatedValueVat(estimatedValue(row));
            contract.setContractValueVat(contractValue(row));
            contract.setCurrency(ValueParser.detectCurrency(firstText(
                    row.get("Валута", "currency"),
                    row.fields().toString()
            )));
            contract.setSourceUrl(row.sourceUrl());

            Contract saved = contractRepository.save(contract);

            if (existing.isEmpty()) {
                result.setContractsImported(result.getContractsImported() + 1);
            }

            syncContractDecisionLink(saved);
            syncContractRealizedContractLink(saved);
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

            if (!isValidNoticeNumber(noticeNumber) || !hasText(institutionName)) {
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
        officialName = cleanInstitutionName(firstText(
                row.get("_institutionOfficialName"),
                row.get("Назив на договорниот орган"),
                officialName
        ));

        if (!hasText(officialName)) {
            throw new IllegalArgumentException("Institution officialName is missing for row: " + row.sourceUrl());
        }

        String normalized = TextNormalizer.normalizeName(officialName);

        Optional<Institution> existingOpt = institutionRepository.findFirstByNormalizedName(normalized);

        if (existingOpt.isPresent()) {
            Institution existing = existingOpt.get();

            boolean changed = false;

            changed |= fillIfBlank(existing::getExternalId, existing::setExternalId,
                    cleanScrapedText(row.get("externalId", "ID", "Шифра")));

            changed |= fillIfBlank(existing::getOfficialName, existing::setOfficialName,
                    TextNormalizer.safe(officialName));

            changed |= fillIfBlank(existing::getInstitutionType, existing::setInstitutionType,
                    cleanScrapedText(row.get("Тип на институција", "institutionType")));

            changed |= fillIfBlank(existing::getCity, existing::setCity,
                    cleanScrapedText(row.get("_institutionCity", "Град", "city")));

            changed |= fillIfBlank(existing::getPostalCode, existing::setPostalCode,
                    cleanScrapedText(row.get("_institutionPostalCode", "Поштенски код", "Поштенски број", "postalCode")));

            changed |= fillIfBlank(existing::getCategory, existing::setCategory,
                    cleanScrapedText(row.get("_institutionCategory", "Категорија", "category")));

            String betterSourceUrl = firstText(row.get("_parentSourceUrl"), row.sourceUrl());

            if (hasText(betterSourceUrl) && !betterSourceUrl.equals(existing.getSourceUrl())) {
                existing.setSourceUrl(betterSourceUrl);
                changed = true;
            }

            if (changed) {
                return institutionRepository.save(existing);
            }

            return existing;
        }

        Institution institution = Institution.builder()
                .externalId(cleanScrapedText(row.get("externalId", "ID", "Шифра")))
                .officialName(TextNormalizer.safe(officialName))
                .normalizedName(normalized)
                .institutionType(cleanScrapedText(row.get("Тип на институција", "institutionType")))
                .city(cleanScrapedText(row.get("_institutionCity", "Град", "city")))
                .postalCode(cleanScrapedText(row.get("_institutionPostalCode", "Поштенски код", "Поштенски број", "postalCode")))
                .category(cleanScrapedText(row.get("_institutionCategory", "Категорија", "category")))
                .sourceUrl(firstText(row.get("_parentSourceUrl"), row.sourceUrl()))
                .build();

        result.setInstitutionsImported(result.getInstitutionsImported() + 1);
        return institutionRepository.save(institution);
    }

    private String cleanInstitutionName(String value) {
        value = cleanScrapedText(value);

        if (!hasText(value)) {
            return value;
        }

        return value
                .replaceFirst("(?i)^\\s*(?:I|1)\\.1\\.1\\)\\s*", "")
                .replaceFirst("(?i)^\\s*Назив на договорниот орган\\s*:?\\s*", "")
                .replaceFirst("(?i)^\\s*Договорен орган\\s*:?\\s*", "")
                .replaceFirst("(?i)^\\s*Институција\\s*:?\\s*", "")
                .trim();
    }

    private Supplier getOrCreateSupplier(ScrapedRow row, String officialName, ImportResultDto result) {
        String normalized = TextNormalizer.normalizeName(officialName);

        return supplierRepository.findFirstByNormalizedName(normalized)
                .orElseGet(() -> {
                    Supplier supplier = Supplier.builder()
                            .externalId(cleanScrapedText(row.get("externalId", "ID", "Шифра")))
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
        Notice notice = noticeRepository.findFirstByNoticeNumber(noticeNumber)
                .orElseGet(() -> Notice.builder()
                        .noticeNumber(noticeNumber)
                        .build());

        if (notice.getInstitution() == null && institution != null) {
            notice.setInstitution(institution);
        }

        if (!hasText(notice.getSubject()) && hasText(subject)) {
            notice.setSubject(subject);
        }

        if (!hasText(notice.getSourceUrl()) && hasText(sourceUrl)) {
            notice.setSourceUrl(sourceUrl);
        }

        return noticeRepository.save(notice);
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
        return cleanInstitutionName(firstText(
                row.get("_institutionOfficialName"),
                row.get("_parentInstitution"),
                row.get(
                        "Назив на договорниот орган",
                        "Договорен орган",
                        "Институција",
                        "Contracting authority",
                        "Institution",
                        "institution"
                )
        ));
    }

    private String supplierName(ScrapedRow row) {
        return cleanScrapedText(row.get(
                "Носител на набавката",
                "Носител на набавка",
                "Добавувач",
                "Економски оператор",
                "Избран понудувач",
                "Оператор",
                "Supplier",
                "supplier"
        ));
    }

    private String noticeNumber(ScrapedRow row) {
        String raw = cleanScrapedText(row.get(
                "Број на оглас",
                "Бр. на оглас",
                "Оглас број",
                "Број на постапка",
                "Број на оглас/постапка",
                "noticeNumber",
                "brojNaOglas",
                "Број"
        ));

        return normalizeNoticeNumber(raw);
    }

    private String subject(ScrapedRow row) {
        return cleanScrapedText(row.get(
                "Предмет на договорот",
                "Предмет на договорот за јавна набавка",
                "Предмет на набавка",
                "Предмет",
                "Опис",
                "subject"
        ));
    }

    private String contractType(ScrapedRow row) {
        return cleanScrapedText(row.get("Вид на договор за јавна набавка", "Вид на договор", "contractType"));
    }

    private String procedureType(ScrapedRow row) {
        return cleanScrapedText(row.get("Вид на постапка", "Постапка", "procedureType"));
    }

    private String realOwners(ScrapedRow row) {
        return cleanScrapedText(row.get("Вистински сопственици", "Податоци за сопственици", "realOwnersInfo"));
    }

    private LocalDate publicationDate(ScrapedRow row) {
        return ValueParser.parseDate(cleanScrapedText(row.get("Датум на објава", "Датум на објавување", "publicationDate")));
    }

    private LocalDateTime deadlineDate(ScrapedRow row) {
        return ValueParser.parseDateTime(cleanScrapedText(row.get("Краен рок", "Рок за поднесување", "deadlineDate")));
    }

    private LocalDate decisionDate(ScrapedRow row) {
        return ValueParser.parseDate(cleanScrapedText(row.get("Датум на одлука", "Датум на избор", "decisionDate")));
    }

    private LocalDate contractDate(ScrapedRow row) {
        return ValueParser.parseDate(cleanScrapedText(row.get("Датум на договор", "Датум на склучување на договор", "contractDate")));
    }

    private LocalDate republishDate(ScrapedRow row) {
        return ValueParser.parseDate(cleanScrapedText(row.get("Датум на повторна објава", "republishDate")));
    }

    private BigDecimal estimatedValue(ScrapedRow row) {
        return ValueParser.parseMoney(cleanScrapedText(row.get(
                "Проценета вредност на набавката со ДДВ",
                "Проценета вредност",
                "estimatedValueVat"
        )));
    }

    private BigDecimal contractValue(ScrapedRow row) {
        return ValueParser.parseMoney(cleanScrapedText(row.get(
                "Вредност на договорот со ДДВ",
                "Вредност на договор",
                "Вкупна вредност со ДДВ",
                "contractValueVat"
        )));
    }

    private BigDecimal awardedValue(ScrapedRow row) {
        return ValueParser.parseMoney(cleanScrapedText(row.get("Доделена вредност", "awardedValueVat")));
    }

    private BigDecimal realizedValue(ScrapedRow row) {
        return ValueParser.parseMoney(cleanScrapedText(row.get("Реализирана вредност", "realizedValueVat")));
    }

    private BigDecimal paidValue(ScrapedRow row) {
        return ValueParser.parseMoney(cleanScrapedText(row.get("Платена вредност", "paidValueVat")));
    }

    private Boolean parseBoolean(String value) {
        if (value == null) {
            return false;
        }

        String normalized = TextNormalizer.normalizeKey(cleanScrapedText(value));

        return normalized.equals("1")
                || normalized.equals("true")
                || normalized.equals("yes")
                || normalized.equals("да")
                || normalized.contains("true")
                || normalized.contains("yes")
                || normalized.contains("да")
                || normalized.contains("има")
                || normalized.contains("notice")
                || normalized.contains("оглас");
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

    private Optional<Contract> findContractForContractImport(
            String sourceUrl,
            String noticeNumber,
            Long institutionId,
            Long supplierId,
            String subject
    ) {
        if (hasText(sourceUrl)) {
            Optional<Contract> foundBySourceUrl = contractRepository.findFirstBySourceUrl(sourceUrl);

            if (foundBySourceUrl.isPresent()) {
                return foundBySourceUrl;
            }
        }

        if (hasText(noticeNumber) && institutionId != null && supplierId != null && hasText(subject)) {
            return contractRepository.findFirstByNoticeNumberAndInstitutionIdAndSupplierIdAndSubjectContainingIgnoreCase(
                    noticeNumber,
                    institutionId,
                    supplierId,
                    TextNormalizer.firstPart(subject, 80)
            );
        }

        return Optional.empty();
    }

    @Transactional
    protected void linkProcurementLifecycle() {
        List<Contract> contracts = contractRepository.findAll();

        for (Contract contract : contracts) {
            String noticeNumber = normalizeNoticeNumber(contract.getNoticeNumber());

            if (!hasText(noticeNumber)) {
                continue;
            }

            Notice notice = noticeRepository
                    .findFirstByNoticeNumber(noticeNumber)
                    .orElse(null);

            if (notice != null) {
                contract.setNotice(notice);

                if (contract.getInstitution() == null && notice.getInstitution() != null) {
                    contract.setInstitution(notice.getInstitution());
                }
            }

            Contract saved = contractRepository.save(contract);

            syncContractDecisionLink(saved);
            syncContractRealizedContractLink(saved);
        }

        List<RealizedContract> realizedContracts = realizedContractRepository.findAll();

        for (RealizedContract realizedContract : realizedContracts) {
            Contract contract = findMatchingContractForRealizedContract(realizedContract)
                    .orElse(null);

            if (contract == null) {
                if (realizedContract.getContract() != null) {
                    realizedContract.setContract(null);
                    realizedContractRepository.save(realizedContract);
                }

                continue;
            }

            clearOtherRealizedContractsLinkedToContract(contract, realizedContract.getId());

            realizedContract.setContract(contract);

            if (realizedContract.getInstitution() == null && contract.getInstitution() != null) {
                realizedContract.setInstitution(contract.getInstitution());
            }

            if (realizedContract.getSupplier() == null && contract.getSupplier() != null) {
                realizedContract.setSupplier(contract.getSupplier());
            }

            realizedContractRepository.save(realizedContract);

        }
    }

    private boolean supplierNamesMatch(Supplier first, Supplier second) {
        if (first == null || second == null) {
            return false;
        }

        String firstName = TextNormalizer.normalizeName(firstText(
                first.getNormalizedName(),
                first.getOfficialName()
        ));

        String secondName = TextNormalizer.normalizeName(firstText(
                second.getNormalizedName(),
                second.getOfficialName()
        ));

        return hasText(firstName) && firstName.equals(secondName);
    }

    private Optional<Contract> findMatchingContractForRealizedContract(RealizedContract realizedContract) {
        String noticeNumber = normalizeNoticeNumber(realizedContract.getNoticeNumber());
        Supplier supplier = realizedContract.getSupplier();

        if (!hasText(noticeNumber) || supplier == null) {
            return Optional.empty();
        }

        if (supplier.getId() != null) {
            Optional<Contract> exactSupplierMatch =
                    contractRepository.findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
                            noticeNumber,
                            supplier.getId()
                    );

            if (exactSupplierMatch.isPresent()) {
                return exactSupplierMatch;
            }
        }

        return contractRepository.findByNoticeNumberIgnoreCaseOrderByIdAsc(noticeNumber)
                .stream()
                .filter(contract -> supplierNamesMatch(contract.getSupplier(), supplier))
                .findFirst();
    }

    private Optional<RealizedContract> findMatchingRealizedContractForContract(Contract contract) {
        String noticeNumber = normalizeNoticeNumber(contract.getNoticeNumber());
        Supplier supplier = contract.getSupplier();

        if (!hasText(noticeNumber) || supplier == null) {
            return Optional.empty();
        }

        if (supplier.getId() != null) {
            Optional<RealizedContract> exactSupplierMatch =
                    realizedContractRepository.findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
                            noticeNumber,
                            supplier.getId()
                    );

            if (exactSupplierMatch.isPresent()) {
                return exactSupplierMatch;
            }
        }

        return realizedContractRepository.findByNoticeNumberIgnoreCaseOrderByIdAsc(noticeNumber)
                .stream()
                .filter(realizedContract -> supplierNamesMatch(realizedContract.getSupplier(), supplier))
                .findFirst();
    }

    private Optional<RealizedContract> findExistingRealizedContractForImport(
            String noticeNumber,
            Supplier supplier,
            Contract contract
    ) {
        if (hasText(noticeNumber) && supplier != null && supplier.getId() != null) {
            Optional<RealizedContract> exactSupplierMatch =
                    realizedContractRepository.findFirstByNoticeNumberIgnoreCaseAndSupplierIdOrderByIdAsc(
                            noticeNumber,
                            supplier.getId()
                    );

            if (exactSupplierMatch.isPresent()) {
                return exactSupplierMatch;
            }
        }

        if (hasText(noticeNumber) && supplier != null) {
            Optional<RealizedContract> supplierNameMatch =
                    realizedContractRepository.findByNoticeNumberIgnoreCaseOrderByIdAsc(noticeNumber)
                            .stream()
                            .filter(realizedContract -> supplierNamesMatch(realizedContract.getSupplier(), supplier))
                            .findFirst();

            if (supplierNameMatch.isPresent()) {
                return supplierNameMatch;
            }
        }

        if (contract != null && contract.getId() != null) {
            return realizedContractRepository.findByContractId(contract.getId())
                    .filter(realizedContract ->
                            supplierNamesMatch(realizedContract.getSupplier(), supplier)
                                    && normalizeNoticeNumber(realizedContract.getNoticeNumber())
                                    .equalsIgnoreCase(normalizeNoticeNumber(noticeNumber))
                    );
        }

        return Optional.empty();
    }

    private void clearOtherRealizedContractsLinkedToContract(Contract contract, Long keepRealizedContractId) {
        if (contract == null || contract.getId() == null) {
            return;
        }

        for (RealizedContract linkedRealizedContract :
                realizedContractRepository.findByContractIdOrderByIdAsc(contract.getId())) {

            if (!java.util.Objects.equals(linkedRealizedContract.getId(), keepRealizedContractId)) {
                linkedRealizedContract.setContract(null);
                realizedContractRepository.save(linkedRealizedContract);
            }
        }
    }

    private void syncContractRealizedContractLink(Contract contract) {
        if (contract == null || contract.getId() == null) {
            return;
        }

        Optional<RealizedContract> matchingRealizedContract =
                findMatchingRealizedContractForContract(contract);

        Long matchingRealizedContractId =
                matchingRealizedContract.map(RealizedContract::getId).orElse(null);

        clearOtherRealizedContractsLinkedToContract(contract, matchingRealizedContractId);

        matchingRealizedContract.ifPresent(realizedContract -> {
            Contract alreadyLinkedContract = realizedContract.getContract();

            if (alreadyLinkedContract == null
                    || !java.util.Objects.equals(alreadyLinkedContract.getId(), contract.getId())) {

                realizedContract.setContract(contract);

                if (realizedContract.getInstitution() == null && contract.getInstitution() != null) {
                    realizedContract.setInstitution(contract.getInstitution());
                }

                if (realizedContract.getSupplier() == null && contract.getSupplier() != null) {
                    realizedContract.setSupplier(contract.getSupplier());
                }

                realizedContractRepository.save(realizedContract);
            }
        });
    }

    private String normalizeNoticeNumber(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return cleanScrapedText(value)
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean fillIfBlank(
            java.util.function.Supplier<String> getter,
            Consumer<String> setter,
            String newValue
    ) {
        if (!hasText(getter.get()) && hasText(newValue)) {
            setter.accept(newValue.trim());
            return true;
        }

        return false;
    }

    private static String cleanScrapedText(String value) {
        if (!hasText(value)) {
            return value;
        }

        String fixed = MojibakeFixer.fix(value);

        if (fixed == null) {
            fixed = value;
        }

        return fixed
                .replace("\u00A0", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}