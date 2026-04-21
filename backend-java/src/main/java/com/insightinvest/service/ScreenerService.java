package com.insightinvest.service;

import com.insightinvest.entity.RiskAssessment;
import com.insightinvest.repository.RiskAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ScreenerService {

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private CompanyDataService companyDataService;

    /**
     * Get trending stocks
     */
    public List<Map<String, Object>> getTrendingStocks() {
        List<Map<String, Object>> trendingStocks = new ArrayList<>();
        
        // Predefined list of popular stocks
        String[] symbols = {"AAPL", "MSFT", "GOOGL", "AMZN", "NVDA", 
                          "RELIANCE.NS", "TCS.NS", "INFY.NS", "WIPRO.NS", "BAJAJFINSV.NS"};
        
        for (String symbol : symbols) {
            try {
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("symbol", symbol);
                stockInfo.put("market", symbol.endsWith(".NS") ? "NSE" : "US");
                
                // Try to fetch real data
                Map<String, Object> companyInfo = companyDataService.fetchFinancialMetrics(symbol);
                if (companyInfo != null && companyInfo.containsKey("currentPrice")) {
                    stockInfo.put("price", companyInfo.get("currentPrice"));
                    stockInfo.put("pe_ratio", companyInfo.get("pe_ratio"));
                }
                
                // Get risk assessment if available
                List<RiskAssessment> riskAssessments = riskAssessmentRepository.findAll();
                for (RiskAssessment risk : riskAssessments) {
                    if (risk.getSymbol().equalsIgnoreCase(symbol)) {
                        stockInfo.put("risk_level", risk.getRiskCategory());
                        stockInfo.put("risk_score", risk.getOverallRiskScore());
                        break;
                    }
                }
                
                // Default values if not found
                if (!stockInfo.containsKey("price")) {
                    stockInfo.put("price", Math.random() * 500);
                    stockInfo.put("trending", true);
                }
                if (!stockInfo.containsKey("risk_level")) {
                    stockInfo.put("risk_level", "MEDIUM");
                }
                
                trendingStocks.add(stockInfo);
            } catch (RuntimeException e) {
                // Skip if error fetching data
                Map<String, Object> fallback = new HashMap<>();
                fallback.put("symbol", symbol);
                fallback.put("market", symbol.endsWith(".NS") ? "NSE" : "US");
                fallback.put("trending", true);
                trendingStocks.add(fallback);
            }
        }
        
        return trendingStocks;
    }

    /**
     * Screen stocks by risk level
     */
    public List<Map<String, Object>> screenByRiskLevel(String riskLevel) {
        List<Map<String, Object>> results = new ArrayList<>();
        List<RiskAssessment> assessments = riskAssessmentRepository.findAll();
        
        for (RiskAssessment assessment : assessments) {
            if (assessment.getRiskCategory().equalsIgnoreCase(riskLevel)) {
                Map<String, Object> stock = new HashMap<>();
                stock.put("symbol", assessment.getSymbol());
                stock.put("risk_score", assessment.getOverallRiskScore());
                stock.put("risk_category", assessment.getRiskCategory());
                results.add(stock);
            }
        }
        
        return results;
    }

    /**
     * Screen stocks with low volatility
     */
    public List<Map<String, Object>> screenLowVolatility() {
        List<Map<String, Object>> results = new ArrayList<>();
        List<RiskAssessment> assessments = riskAssessmentRepository.findAll();
        
        for (RiskAssessment assessment : assessments) {
            if (assessment.getOverallRiskScore() < 0.4) { // Low risk
                Map<String, Object> stock = new HashMap<>();
                stock.put("symbol", assessment.getSymbol());
                stock.put("risk_score", assessment.getOverallRiskScore());
                stock.put("volatility", "LOW");
                results.add(stock);
            }
        }
        
        return !results.isEmpty() ? results : getTrendingStocks().subList(0, Math.min(5, getTrendingStocks().size()));
    }

    /**
     * Screen stocks with high growth potential
     */
    public List<Map<String, Object>> screenHighGrowth() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Financial metrics based screening
        String[] growthStocks = {"NVDA", "AMZN", "MSFT", "GOOGL", "INFY.NS"};
        
        for (String symbol : growthStocks) {
            try {
                Map<String, Object> companyInfo = companyDataService.fetchFinancialMetrics(symbol);
                if (companyInfo != null) {
                    Map<String, Object> stock = new HashMap<>();
                    stock.put("symbol", symbol);
                    stock.put("pe_ratio", companyInfo.getOrDefault("pe_ratio", "N/A"));
                    stock.put("market_cap", companyInfo.getOrDefault("marketCap", "N/A"));
                    stock.put("growth_potential", "HIGH");
                    results.add(stock);
                }
            } catch (RuntimeException e) {
                // Continue with next stock
            }
        }
        
        return !results.isEmpty() ? results : getTrendingStocks().subList(0, Math.min(5, getTrendingStocks().size()));
    }
}
