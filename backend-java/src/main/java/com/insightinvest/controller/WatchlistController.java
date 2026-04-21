package com.insightinvest.controller;

import com.insightinvest.entity.Watchlist;
import com.insightinvest.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/watchlist")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    /**
     * Add symbol to watchlist
     * POST /watchlist/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToWatchlist(@RequestBody Map<String, Object> request) {
        try {
            Long investorId = ((Number) request.get("investorId")).longValue();
            String symbol = (String) request.get("symbol");

            if (symbol == null || symbol.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Symbol is required"));
            }

            Watchlist watchlist = watchlistService.addToWatchlist(investorId, symbol);

            Map<String, Object> response = new HashMap<>();
            response.put("watchlistId", watchlist.getWatchlistId());
            response.put("symbol", watchlist.getSymbol());
            response.put("investorId", watchlist.getInvestor().getUserId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all watchlist items for an investor
     * GET /watchlist?investorId={investorId}
     */
    @GetMapping
    public ResponseEntity<?> getWatchlist(@RequestParam Long investorId) {
        try {
            List<Watchlist> watchlist = watchlistService.getWatchlistByInvestor(investorId);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove symbol from watchlist
     * DELETE /watchlist/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromWatchlist(@RequestBody Map<String, Object> request) {
        try {
            Long investorId = ((Number) request.get("investorId")).longValue();
            String symbol = (String) request.get("symbol");

            if (symbol == null || symbol.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Symbol is required"));
            }

            watchlistService.removeFromWatchlist(investorId, symbol);
            return ResponseEntity.ok(Map.of("message", "Symbol removed from watchlist"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if symbol is in watchlist
     * GET /watchlist/check?investorId={investorId}&symbol={symbol}
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkWatchlist(@RequestParam Long investorId, @RequestParam String symbol) {
        try {
            boolean isInWatchlist = watchlistService.isInWatchlist(investorId, symbol);
            Map<String, Object> response = new HashMap<>();
            response.put("investorId", investorId);
            response.put("symbol", symbol);
            response.put("isInWatchlist", isInWatchlist);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
