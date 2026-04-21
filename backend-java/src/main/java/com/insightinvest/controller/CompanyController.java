package com.insightinvest.controller;

import com.insightinvest.dto.ForecastResponse;
import com.insightinvest.service.AnalysisFacadeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping
public class CompanyController {

    private final AnalysisFacadeService analysisFacadeService;

    public CompanyController(AnalysisFacadeService analysisFacadeService) {
        this.analysisFacadeService = analysisFacadeService;
    }

    @GetMapping("/forecast/{symbol}")
    public ResponseEntity<?> getForecast(@PathVariable String symbol,
                                         @RequestParam(defaultValue = "10") int steps,
                                         @RequestParam(defaultValue = "6mo") String period,
                                         @RequestParam(name = "news_items", defaultValue = "15") int newsItems,
                                         HttpServletRequest request) {
        try {
            int safeSteps = Math.min(30, Math.max(5, steps));
            int safeNewsItems = Math.min(25, Math.max(5, newsItems));

            ForecastResponse response = analysisFacadeService.buildComprehensiveForecast(symbol, safeSteps, period, safeNewsItems);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Analysis failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "InsightInvest - Java Financial Analyst");
        response.put("version", "3.0.0-java");
        response.put("status", "operational");
        response.put("endpoints", Map.of(
                "main_analysis", "/forecast/{symbol}",
                "health", "/actuator/health"
        ));
        response.put("supported_parameters", Map.of(
                "steps", "5-30",
                "period", "1mo, 3mo, 6mo, 1y, 2y",
                "news_items", "5-25"
        ));
        return ResponseEntity.ok(response);
    }
}
