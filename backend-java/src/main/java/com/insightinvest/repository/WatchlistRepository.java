package com.insightinvest.repository;

import com.insightinvest.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    List<Watchlist> findByInvestor_UserId(Long investorId);
    Optional<Watchlist> findByInvestor_UserIdAndSymbol(Long investorId, String symbol);
}
