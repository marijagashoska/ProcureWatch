package com.procurewatchbackend.config;

import com.procurewatchbackend.repository.ContractRepository;
import com.procurewatchbackend.repository.RiskAssessmentRepository;
import com.procurewatchbackend.service.domain.RiskAssessmentDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
@RequiredArgsConstructor
public class H2DemoDataRiskRunner implements CommandLineRunner {

    private final ContractRepository contractRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final RiskAssessmentDomainService riskAssessmentDomainService;

    @Override
    public void run(String... args) {
        if (contractRepository.count() == 0) {
            return;
        }

        if (riskAssessmentRepository.count() > 0) {
            return;
        }

        riskAssessmentDomainService.evaluateAllContracts();
    }
}