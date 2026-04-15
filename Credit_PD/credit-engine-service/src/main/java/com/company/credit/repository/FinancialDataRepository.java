package com.company.credit.repository;

import com.company.credit.domain.FinancialData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FinancialDataRepository extends JpaRepository<FinancialData, UUID> {
    Optional<FinancialData> findTopByUserIdOrderByCreatedAtDesc(String userId);
}
