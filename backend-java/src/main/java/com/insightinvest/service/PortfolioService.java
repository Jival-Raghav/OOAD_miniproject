package com.insightinvest.service;

import com.insightinvest.entity.Holding;
import com.insightinvest.entity.Investor;
import com.insightinvest.entity.Portfolio;
import com.insightinvest.repository.HoldingRepository;
import com.insightinvest.repository.PortfolioRepository;
import com.insightinvest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private HoldingRepository holdingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new portfolio
     */
    public Portfolio createPortfolio(Long investorId, Map<String, Object> request) {
        Investor investor = (Investor) userRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setInvestor(investor);
        portfolio.setTotalValue(request.containsKey("totalValue")
            ? ((Number) request.get("totalValue")).doubleValue()
            : 0.0);

        return portfolioRepository.save(portfolio);
    }

    /**
     * Get all portfolios for an investor
     */
    public List<Portfolio> getPortfoliosByInvestor(Long investorId) {
        return portfolioRepository.findByInvestor_UserId(investorId);
    }

    /**
     * Get portfolio by ID
     */
    public Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
    }

    /**
     * Update portfolio
     */
    public Portfolio updatePortfolio(Long portfolioId, Map<String, Object> updateData) {
        Portfolio portfolio = getPortfolioById(portfolioId);

        if (updateData.containsKey("totalValue")) {
            portfolio.setTotalValue(((Number) updateData.get("totalValue")).doubleValue());
        }

        return portfolioRepository.save(portfolio);
    }

    /**
     * Delete portfolio
     */
    public void deletePortfolio(Long portfolioId) {
        Portfolio portfolio = getPortfolioById(portfolioId);
        
        // Delete all holdings first
        List<Holding> holdings = holdingRepository.findByPortfolio_PortfolioId(portfolioId);
        holdingRepository.deleteAll(holdings);
        
        // Delete portfolio
        portfolioRepository.delete(portfolio);
    }

    /**
     * Add holding to portfolio
     */
    public Holding addHolding(Long portfolioId, Map<String, Object> holdingData) {
        Portfolio portfolio = getPortfolioById(portfolioId);

        String symbol = (String) holdingData.get("symbol");
        Integer quantity = ((Number) holdingData.get("quantity")).intValue();
        Double purchasePrice = ((Number) holdingData.get("purchasePrice")).doubleValue();

        // Check if holding already exists
        java.util.Optional<Holding> existingHolding = holdingRepository
                .findByPortfolio_PortfolioIdAndSymbol(portfolioId, symbol);

        if (existingHolding.isPresent()) {
            throw new RuntimeException("Holding already exists in this portfolio");
        }

        Holding holding = new Holding();
        holding.setPortfolio(portfolio);
        holding.setSymbol(symbol);
        holding.setQuantity(quantity);
        holding.setPurchasePrice(purchasePrice);

        return holdingRepository.save(holding);
    }

    /**
     * Update holding
     */
    public Holding updateHolding(Long holdingId, Map<String, Object> updateData) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));

        if (updateData.containsKey("quantity")) {
            holding.setQuantity(((Number) updateData.get("quantity")).intValue());
        }
        if (updateData.containsKey("purchasePrice")) {
            holding.setPurchasePrice(((Number) updateData.get("purchasePrice")).doubleValue());
        }

        return holdingRepository.save(holding);
    }

    /**
     * Remove holding from portfolio
     */
    public void removeHolding(Long holdingId) {
        Holding holding = holdingRepository.findById(holdingId)
                .orElseThrow(() -> new RuntimeException("Holding not found"));
        holdingRepository.delete(holding);
    }

    /**
     * Get all holdings in a portfolio
     */
    public List<Holding> getHoldingsByPortfolio(Long portfolioId) {
        return holdingRepository.findByPortfolio_PortfolioId(portfolioId);
    }
}
