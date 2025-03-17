package org.swmaestro.repl.gifthub.notifications.config;

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
import org.springframework.transaction.PlatformTransactionManager;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationItemWriter;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationItemProcessor;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationItemReader;
import org.swmaestro.repl.gifthub.notifications.service.VoucherExpirationJobListener;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

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
    private final VoucherService voucherService;
    private final FCMNotificationService fcmNotificationService;
    private final VoucherExpirationJobListener jobListener;

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
        return new StepBuilder("voucherExpirationNotificationStep", jobRepository)
                .<Voucher, FCMNotificationRequestDto>chunk(chunkSize, transactionManager)
                .reader(voucherExpirationItemReader(voucherService))
                .processor(voucherExpirationItemProcessor())
                .writer(fcmNotificationItemWriter(fcmNotificationService))
                .taskExecutor(voucherNotificationTaskExecutor)  // 멀티스레딩 적용
                .build();
    }

    /**
     * 만료 예정 모바일 상품권 ItemReader 빈 생성
     * @param voucherService 바우처 서비스
     * @return 구성된 ItemReader
     */
    @Bean
    public VoucherExpirationItemReader voucherExpirationItemReader(VoucherService voucherService) {
        return new VoucherExpirationItemReader(voucherService);
    }

    /**
     * 만료 예정 모바일 상품권 ItemProcessor 빈 생성
     * @return 구성된 ItemProcessor
     */
    @Bean
    public VoucherExpirationItemProcessor voucherExpirationItemProcessor() {
        return new VoucherExpirationItemProcessor();
    }

    /**
     * 만료 예정 모바일 상품권 ItemWriter 빈 생성
     * FCM 알림 전송을 담당
     * @return 구성된 ItemWriter
     */
    @Bean
    public FCMNotificationItemWriter fcmNotificationItemWriter(FCMNotificationService fcmNotificationService) {
        return new FCMNotificationItemWriter(fcmNotificationService);
    }
}

