package com.insightinvest.service;

import com.insightinvest.entity.Investor;
import com.insightinvest.entity.Watchlist;
import com.insightinvest.repository.UserRepository;
import com.insightinvest.repository.WatchlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Add symbol to watchlist
     */
    public Watchlist addToWatchlist(Long investorId, String symbol) {
        Investor investor = (Investor) userRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found"));

        // Check if already in watchlist
        if (watchlistRepository.findByInvestor_UserIdAndSymbol(investorId, symbol).isPresent()) {
            throw new RuntimeException("Symbol already in watchlist");
        }

        Watchlist watchlist = new Watchlist();
        watchlist.setInvestor(investor);
        watchlist.setSymbol(symbol);

        return watchlistRepository.save(watchlist);
    }

    /**
     * Get all watchlist items for an investor
     */
    public List<Watchlist> getWatchlistByInvestor(Long investorId) {
        return watchlistRepository.findByInvestor_UserId(investorId);
    }

    /**
     * Remove symbol from watchlist
     */
    public void removeFromWatchlist(Long investorId, String symbol) {
        Watchlist watchlist = watchlistRepository.findByInvestor_UserIdAndSymbol(investorId, symbol)
                .orElseThrow(() -> new RuntimeException("Symbol not found in watchlist"));
        watchlistRepository.delete(watchlist);
    }

    /**
     * Check if symbol is in watchlist
     */
    public boolean isInWatchlist(Long investorId, String symbol) {
        return watchlistRepository.findByInvestor_UserIdAndSymbol(investorId, symbol).isPresent();
    }
}
