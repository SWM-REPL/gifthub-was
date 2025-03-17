package org.swmaestro.repl.gifthub.notifications.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationItemWriter;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationItemProcessor;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationItemReader;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationJobListener;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import com.google.firebase.messaging.FirebaseMessagingException;

import lombok.RequiredArgsConstructor;

/***
 * 모바일 상품권 만료 알림 배치 설정 클래스
 * 목적: Job, Step 및 관련 컴포넌트 정의
 */
@Configuration
@RequiredArgsConstructor
public class VoucherExpirationBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FCMNotificationService fcmNotificationService;
    private final VoucherExpirationJobListener jobListener;
    private final VoucherExpirationItemReader voucherExpirationItemReader;
    private final VoucherExpirationItemProcessor voucherExpirationItemProcessor;

    @Qualifier("voucherNotificationTaskExecutor")
    private final TaskExecutor voucherNotificationTaskExecutor;

    @Value("${gifthub.batch.notification.chunk-size:50}")
    private int chunkSize;

    /***
     * 모바일 상품권 만료 알림 배치 Job 정의
     * @param voucherExpirationNotificationStep 실행할 Step
     * @return 구성된 Job 객체
     */
    @Bean
    public Job voucherExpirationNotificationJob(Step voucherExpirationNotificationStep) {
        return new JobBuilder("voucherExpirationNotificationJob", jobRepository)
                .listener(jobListener)
                .start(voucherExpirationNotificationStep)
                .build();
    }

    /**
     * 모바일 상품권 만료 알림을 위한 Step 정의
     * 멀티스레딩 처리를 위한 TaskExecutor 설정 추가
     * @return 구성된 Step
     */
    @Bean
    public Step voucherExpirationNotificationStep() {
        FCMNotificationItemWriter itemWriter = new FCMNotificationItemWriter(fcmNotificationService, fcmRetryTemplate());

        return new StepBuilder("voucherExpirationNotificationStep", jobRepository)
                .<Voucher, FCMNotificationRequestDto>chunk(chunkSize, transactionManager)
                .reader(voucherExpirationItemReader)
                .processor(voucherExpirationItemProcessor)
                .writer(itemWriter)
                .taskExecutor(voucherNotificationTaskExecutor)  // 멀티스레딩 적용
                .build();
    }

    /**
     * FCM 전송 재시도를 위한 RetryTemplate 빈 생성
     * @return 구성된 RetryTemplate
     */
    @Bean
    public RetryTemplate fcmRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 재시도 정책 설정 (3회 시도)
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(FirebaseMessagingException.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);

        // 백오프 정책 설정 (초기 1초, 배수 2의 지수 백오프)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

}

