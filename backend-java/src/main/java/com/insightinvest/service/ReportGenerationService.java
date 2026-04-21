package com.insightinvest.service;

import com.insightinvest.entity.InvestmentReport;
import com.insightinvest.repository.InvestmentReportRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportGenerationService {

    private final InvestmentReportRepository investmentReportRepository;

    public ReportGenerationService(InvestmentReportRepository investmentReportRepository) {
        this.investmentReportRepository = investmentReportRepository;
    }

    public void saveReport(String symbol, String reportText, String recommendation) {
        InvestmentReport report = new InvestmentReport();
        report.setSymbol(symbol);
        report.setAnalysisText(reportText);
        report.setRecommendation(recommendation);
        investmentReportRepository.save(report);
    }
}
