package com.company.credit.service;

import com.company.credit.domain.DataFetchStatus;
import com.company.credit.domain.FinancialData;
import com.company.credit.domain.FinancialMetrics;
import com.company.credit.events.AccountConnectedEvent;
import com.company.credit.events.DataFetchCompletedEvent;
import com.company.credit.integration.mono.MonoClient;
import com.company.credit.integration.mono.MonoFinancialSnapshot;
import com.company.credit.repository.FinancialDataRepository;
import com.company.credit.repository.FinancialMetricsRepository;
import com.company.credit.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialDataIngestionService {

    private static final long[] RETRY_DELAYS_MINUTES = {1, 5, 15, 60};

    private final MonoClient monoClient;
    private final FinancialDataRepository financialDataRepository;
    private final FinancialMetricsRepository financialMetricsRepository;
    private final CryptoUtil cryptoUtil;
    private final TaskScheduler taskScheduler;
    private final ApplicationEventPublisher eventPublisher;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountConnected(AccountConnectedEvent event) {
        fetchAndPersist(event, 0);
    }

    @Transactional
    public void fetchAndPersist(AccountConnectedEvent event, int attempt) {
        String userId = event.userId();
        FinancialData data = new FinancialData();
        data.setUserId(userId);
        data.setStatus(DataFetchStatus.FETCHING);
        data.setRawData(cryptoUtil.encrypt("{}"));
        financialDataRepository.save(data);

        log.info("State transition FinancialData userId={} status={}", userId, DataFetchStatus.FETCHING);

        try {
            MonoFinancialSnapshot snapshot = monoClient.fetchFinancialSnapshot(event.monoAccountId(), event.data());

            data.setRawData(cryptoUtil.encrypt(snapshot.getRawJson()));
            data.setLastSuccessfulSync(Instant.now());
            if (snapshot.getMonthsOfData() < 6) {
                data.setStatus(DataFetchStatus.PARTIAL);
            } else {
                data.setStatus(DataFetchStatus.COMPLETED);
            }
            financialDataRepository.save(data);

            FinancialMetrics metrics = new FinancialMetrics();
            metrics.setUserId(userId);
            metrics.setAverageMonthlyIncome(snapshot.getAverageMonthlyIncome());
            metrics.setIncomeVolatility(snapshot.getIncomeVolatility());
            metrics.setDebtToIncomeRatio(snapshot.getDebtToIncomeRatio());
            metrics.setLowestMonthlyBalance(snapshot.getLowestMonthlyBalance());
            metrics.setMonthsOfData(snapshot.getMonthsOfData());
            metrics.setPaydaySweepRatio(snapshot.getPaydaySweepRatio());
            financialMetricsRepository.save(metrics);

            log.info("State transition FinancialData userId={} status={}", userId, data.getStatus());
            eventPublisher.publishEvent(new DataFetchCompletedEvent(userId, data.getStatus()));
        } catch (Exception ex) {
            data.setStatus(DataFetchStatus.FAILED);
            financialDataRepository.save(data);
            log.warn("Data ingestion failed for userId={} attempt={} error={}", userId, attempt, ex.getMessage());
            if (attempt < RETRY_DELAYS_MINUTES.length) {
                long delay = RETRY_DELAYS_MINUTES[attempt];
                taskScheduler.schedule(() -> fetchAndPersist(event, attempt + 1), Instant.now().plus(delay, ChronoUnit.MINUTES));
            }
        }
    }
}
