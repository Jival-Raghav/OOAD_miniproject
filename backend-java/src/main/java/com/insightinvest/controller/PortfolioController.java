package com.insightinvest.controller;

import com.insightinvest.entity.Holding;
import com.insightinvest.entity.Portfolio;
import com.insightinvest.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolios")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class PortfolioController {

    @Autowired
    private PortfolioService portfolioService;

    /**
     * Create a new portfolio
     * POST /portfolios
     */
    @PostMapping
    public ResponseEntity<?> createPortfolio(@RequestBody Map<String, Object> request) {
        try {
            Long investorId = ((Number) request.get("investorId")).longValue();
            Portfolio portfolio = portfolioService.createPortfolio(investorId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("portfolioId", portfolio.getPortfolioId());
            response.put("totalValue", portfolio.getTotalValue());
            response.put("investorId", portfolio.getInvestor().getUserId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all portfolios for an investor
     * GET /portfolios?investorId={investorId}
     */
    @GetMapping
    public ResponseEntity<?> getPortfolios(@RequestParam Long investorId) {
        try {
            List<Portfolio> portfolios = portfolioService.getPortfoliosByInvestor(investorId);
            return ResponseEntity.ok(portfolios);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get portfolio by ID
     * GET /portfolios/{portfolioId}
     */
    @GetMapping("/{portfolioId}")
    public ResponseEntity<?> getPortfolioById(@PathVariable Long portfolioId) {
        try {
            Portfolio portfolio = portfolioService.getPortfolioById(portfolioId);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update portfolio
     * PUT /portfolios/{portfolioId}
     */
    @PutMapping("/{portfolioId}")
    public ResponseEntity<?> updatePortfolio(@PathVariable Long portfolioId, @RequestBody Map<String, Object> updateData) {
        try {
            Portfolio portfolio = portfolioService.updatePortfolio(portfolioId, updateData);
            return ResponseEntity.ok(portfolio);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete portfolio
     * DELETE /portfolios/{portfolioId}
     */
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<?> deletePortfolio(@PathVariable Long portfolioId) {
        try {
            portfolioService.deletePortfolio(portfolioId);
            return ResponseEntity.ok(Map.of("message", "Portfolio deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Add holding to portfolio
     * POST /portfolios/{portfolioId}/holdings
     */
    @PostMapping("/{portfolioId}/holdings")
    public ResponseEntity<?> addHolding(@PathVariable Long portfolioId, @RequestBody Map<String, Object> holdingData) {
        try {
            Holding holding = portfolioService.addHolding(portfolioId, holdingData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("holdingId", holding.getHoldingId());
            response.put("symbol", holding.getSymbol());
            response.put("quantity", holding.getQuantity());
            response.put("purchasePrice", holding.getPurchasePrice());
            response.put("portfolioId", holding.getPortfolio().getPortfolioId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all holdings in a portfolio
     * GET /portfolios/{portfolioId}/holdings
     */
    @GetMapping("/{portfolioId}/holdings")
    public ResponseEntity<?> getHoldings(@PathVariable Long portfolioId) {
        try {
            List<Holding> holdings = portfolioService.getHoldingsByPortfolio(portfolioId);
            return ResponseEntity.ok(holdings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update holding
     * PUT /portfolios/{portfolioId}/holdings/{holdingId}
     */
    @PutMapping("/{portfolioId}/holdings/{holdingId}")
    public ResponseEntity<?> updateHolding(@PathVariable Long portfolioId, @PathVariable Long holdingId, 
                                          @RequestBody Map<String, Object> updateData) {
        try {
            Holding holding = portfolioService.updateHolding(holdingId, updateData);
            return ResponseEntity.ok(holding);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove holding from portfolio
     * DELETE /portfolios/{portfolioId}/holdings/{holdingId}
     */
    @DeleteMapping("/{portfolioId}/holdings/{holdingId}")
    public ResponseEntity<?> removeHolding(@PathVariable Long portfolioId, @PathVariable Long holdingId) {
        try {
            portfolioService.removeHolding(holdingId);
            return ResponseEntity.ok(Map.of("message", "Holding removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
