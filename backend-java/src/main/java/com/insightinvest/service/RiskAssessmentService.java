package com.insightinvest.service;

import com.insightinvest.entity.RiskAssessment;
import com.insightinvest.repository.RiskAssessmentRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiskAssessmentService {

    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessmentService(RiskAssessmentRepository riskAssessmentRepository) {
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    public Map<String, Object> assessRisk(String symbol, List<Double> prices, double sentimentScore) {
        double volatility = estimateVolatility(prices);
        double sentimentRisk = 1.0 - sentimentScore;
        double riskScore = Math.min(1.0, (volatility * 4.0) + (sentimentRisk * 0.35));

        String category;
        if (riskScore >= 0.7) category = "HIGH";
        else if (riskScore >= 0.4) category = "MEDIUM";
        else category = "LOW";

        RiskAssessment assessment = new RiskAssessment();
        assessment.setSymbol(symbol);
        assessment.setOverallRiskScore(riskScore);
        assessment.setRiskCategory(category);
        riskAssessmentRepository.save(assessment);

        Map<String, Object> out = new HashMap<>();
        out.put("overallRiskScore", riskScore);
        out.put("riskCategory", category);
        return out;
    }

    private static double estimateVolatility(List<Double> prices) {
        if (prices == null || prices.size() < 3) return 0.02;
        double mean = 0.0;
        int count = 0;
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            if (prev == 0) continue;
            mean += (prices.get(i) - prev) / prev;
            count++;
        }
        if (count == 0) return 0.02;
        mean /= count;

        double variance = 0.0;
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            if (prev == 0) continue;
            double r = (prices.get(i) - prev) / prev;
            variance += (r - mean) * (r - mean);
        }
        variance /= count;
        return Math.sqrt(Math.max(variance, 1e-4));
    }
}
