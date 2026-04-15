package com.company.credit.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "financial_metrics")
@Getter
@Setter
public class FinancialMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal averageMonthlyIncome;

    @Column(nullable = false)
    private BigDecimal incomeVolatility;

    @Column(nullable = false)
    private BigDecimal debtToIncomeRatio;

    @Column(nullable = false)
    private BigDecimal lowestMonthlyBalance;

    @Column(nullable = false)
    private int monthsOfData;

    @Column(nullable = false)
    private BigDecimal paydaySweepRatio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
