package com.insightinvest.service;

import com.insightinvest.entity.InvestmentReport;
import com.insightinvest.repository.InvestmentReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private InvestmentReportRepository reportRepository;

    /**
     * Get all reports
     */
    public List<InvestmentReport> getAllReports() {
        return reportRepository.findAll();
    }

    /**
     * Get report by ID
     */
    public InvestmentReport getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }

    /**
     * Get reports by symbol
     */
    public List<InvestmentReport> getReportsBySymbol(String symbol) {
        return reportRepository.findBySymbol(symbol);
    }

    /**
     * Delete report
     */
    public void deleteReport(Long reportId) {
        InvestmentReport report = getReportById(reportId);
        reportRepository.delete(report);
    }

    /**
     * Save a new report
     */
    public InvestmentReport saveReport(String symbol, String recommendation, String analysisText) {
        InvestmentReport report = new InvestmentReport();
        report.setSymbol(symbol);
        report.setRecommendation(recommendation);
        report.setAnalysisText(analysisText);
        report.setCreatedAt(OffsetDateTime.now());
        return reportRepository.save(report);
    }
}
