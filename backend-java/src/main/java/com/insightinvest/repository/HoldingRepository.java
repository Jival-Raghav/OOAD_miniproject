package com.insightinvest.repository;

import com.insightinvest.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findByPortfolio_PortfolioId(Long portfolioId);
    Optional<Holding> findByPortfolio_PortfolioIdAndSymbol(Long portfolioId, String symbol);
}
