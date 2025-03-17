package org.swmaestro.repl.gifthub.notifications.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 *
 */
@Configuration
public class VoucherBatchThreadConfig {
    @Bean
    public TaskExecutor voucherNotificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 가용 프로세서 수를 기반으로 코어 스레드 수 설정
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;

        executor.setCorePoolSize(corePoolSize);                  // 기본 스레드 수
        executor.setMaxPoolSize(corePoolSize * 2);               // 최대 스레드 수
        executor.setQueueCapacity(corePoolSize * 5);             // 작업 대기열 크기
        executor.setThreadNamePrefix("voucher-notification-");   // 스레드 이름 접두사
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 거부 정책
        executor.setKeepAliveSeconds(60);                        // 초과 스레드 유지 시간
        executor.setWaitForTasksToCompleteOnShutdown(true);      // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(60);                 // 종료 시 최대 대기 시간
        executor.initialize();

        return executor;
    }
}
