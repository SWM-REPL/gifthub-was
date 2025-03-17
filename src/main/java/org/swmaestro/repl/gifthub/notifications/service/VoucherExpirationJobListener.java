package org.swmaestro.repl.gifthub.notifications.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 바우처 만료 알림 작업 성능 측정을 위한 리스너
 * 목적: 작업 수행 시간, 처리 항목 수, 메모리 사용량 등 성능 지표 측정
 */
@Component
public class VoucherExpirationJobListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(VoucherExpirationJobListener.class);

    private LocalDateTime startTime;
    private long startMemory;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = LocalDateTime.now();
        startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        logger.info("=======================================");
        logger.info("바우처 만료 알림 작업 시작 시간: {}", startTime);
        logger.info("초기 메모리 사용량: {} MB", startMemory / (1024 * 1024));
        logger.info("=======================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime endTime = LocalDateTime.now();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        Duration duration = Duration.between(startTime, endTime);
        long memoryUsed = (endMemory - startMemory) / (1024 * 1024); // MB

        logger.info("=======================================");
        logger.info("바우처 만료 알림 작업 완료 시간: {}", endTime);
        logger.info("작업 상태: {}", jobExecution.getStatus());
        logger.info("총 실행 시간: {} 초", duration.getSeconds());
        logger.info("추가 메모리 사용량: {} MB", memoryUsed);

        // 성능 지표
        long totalItems = jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getReadCount())
                .sum();

        if (duration.getSeconds() > 0 && totalItems > 0) {
            logger.info("처리율: {} 항목/초", totalItems / duration.getSeconds());
        }

        // 스텝 실행에서 스레드 풀 지표
        jobExecution.getStepExecutions().forEach(stepExecution -> {
            logger.info("스텝: {} - 읽음: {}, 처리됨: {}, 쓰기: {}, 필터링됨: {}, 건너뜀: {}",
                    stepExecution.getStepName(),
                    stepExecution.getReadCount(),
                    stepExecution.getReadCount() - stepExecution.getFilterCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getFilterCount(),
                    stepExecution.getReadSkipCount() + stepExecution.getProcessSkipCount() + stepExecution.getWriteSkipCount());
        });

        logger.info("=======================================");
    }
}

