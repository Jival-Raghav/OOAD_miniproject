package com.insightinvest.service;

import com.insightinvest.dto.ForecastResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastService {

    public ForecastResult generateForecast(List<Double> historicalPrices, int steps, double sentimentScore) {
        if (historicalPrices == null || historicalPrices.size() < 20) {
            throw new IllegalArgumentException("At least 20 price points are required for forecasting");
        }

        List<Double> mean = new ArrayList<>();
        List<Double> lower = new ArrayList<>();
        List<Double> upper = new ArrayList<>();

        int n = historicalPrices.size();
        double first = historicalPrices.get(Math.max(0, n - 30));
        double last = historicalPrices.get(n - 1);
        double slope = (last - first) / Math.max(1, Math.min(29, n - 1));

        double sentimentAdjustment = 1.0 + ((sentimentScore - 0.5) * 0.12);
        double volatility = estimateVolatility(historicalPrices);

        for (int i = 1; i <= steps; i++) {
            double base = last + (slope * i);
            double adjusted = base * sentimentAdjustment;
            double band = Math.max(0.01, adjusted * volatility * Math.sqrt(i) * 0.35);
            mean.add(round(adjusted));
            lower.add(round(Math.max(0.0, adjusted - band)));
            upper.add(round(adjusted + band));
        }

        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("sentiment_adjustment", sentimentAdjustment);
        diagnostics.put("historical_volatility", volatility);
        diagnostics.put("trend_slope", slope);

        ForecastResult result = new ForecastResult();
        result.setMean(mean);
        result.setLower(lower);
        result.setUpper(upper);
        result.setDiagnostics(diagnostics);
        return result;
    }

    private static double estimateVolatility(List<Double> prices) {
        if (prices.size() < 3) return 0.02;
        List<Double> returns = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            double prev = prices.get(i - 1);
            double curr = prices.get(i);
            if (prev > 0) returns.add((curr - prev) / prev);
        }
        if (returns.isEmpty()) return 0.02;
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(r -> (r - mean) * (r - mean)).sum() / returns.size();
        return Math.max(0.01, Math.sqrt(variance));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
