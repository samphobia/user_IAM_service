package com.company.credit.repository;

import com.company.credit.domain.BankAccount;
import com.company.credit.domain.BankAccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository extends JpaRepository<BankAccount, UUID> {
    boolean existsByUserIdAndStatus(String userId, BankAccountStatus status);
    Optional<BankAccount> findByMonoAccountId(String monoAccountId);
}
