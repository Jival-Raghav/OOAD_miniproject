package com.insightinvest.repository;

import com.insightinvest.entity.InvestmentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvestmentReportRepository extends JpaRepository<InvestmentReport, Long> {
    List<InvestmentReport> findBySymbol(String symbol);
}
