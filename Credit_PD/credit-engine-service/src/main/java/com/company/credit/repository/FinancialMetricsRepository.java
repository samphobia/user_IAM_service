package com.company.credit.repository;

import com.company.credit.domain.FinancialMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialMetricsRepository extends JpaRepository<FinancialMetrics, UUID> {
    Optional<FinancialMetrics> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
