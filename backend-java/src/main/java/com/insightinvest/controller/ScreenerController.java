package com.insightinvest.controller;

import com.insightinvest.service.ScreenerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@RestController
@RequestMapping("/screener")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
public class ScreenerController {

    private final ScreenerService screenerService;

    public ScreenerController(ScreenerService screenerService) {
        this.screenerService = screenerService;
    }

    /**
     * Get trending stocks
     * GET /screener/trending
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrending() {
        try {
            return ResponseEntity.ok(Map.of("trending", screenerService.getTrendingStocks()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Screen stocks by risk level
     * GET /screener/by-risk?level={level}
     */
    @GetMapping("/by-risk")
    public ResponseEntity<Map<String, Object>> screenByRisk(@RequestParam String level) {
        try {
            return ResponseEntity.ok(Map.of("results", screenerService.screenByRiskLevel(level)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Screen low volatility stocks
     * GET /screener/low-volatility
     */
    @GetMapping("/low-volatility")
    public ResponseEntity<Map<String, Object>> getLowVolatility() {
        try {
            return ResponseEntity.ok(Map.of("results", screenerService.screenLowVolatility()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Screen high growth stocks
     * GET /screener/high-growth
     */
    @GetMapping("/high-growth")
    public ResponseEntity<Map<String, Object>> getHighGrowth() {
        try {
            return ResponseEntity.ok(Map.of("results", screenerService.screenHighGrowth()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
