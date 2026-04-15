package com.company.credit.repository;

import com.company.credit.domain.CreditDecision;
import com.company.credit.domain.CreditDecisionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CreditDecisionRepository extends JpaRepository<CreditDecision, UUID> {
    Optional<CreditDecision> findTopByUserIdOrderByCreatedAtDesc(String userId);
    Optional<CreditDecision> findTopByUserIdAndStatusOrderByCreatedAtDesc(String userId, CreditDecisionStatus status);
}
