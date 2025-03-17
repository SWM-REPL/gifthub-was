package org.swmaestro.repl.gifthub.notifications.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

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

    /***
     * 모바일 상품권 만료 알림 배치 Job 정의
     * @param voucherExpirationNotificationStep 실행할 Step
     * @return 구성된 Job 객체
     */
    @Bean
    public Job voucherExpirationNotificationJob(Step voucherExpirationNotificationStep) {
        return new JobBuilder("voucherExpirationNotificationJob", jobRepository)
                .start(voucherExpirationNotificationStep)
                .build();
    }

    @Bean
    public Step voucherExpirationNotificationStep() {
        return new StepBuilder("voucherExpirationNotificationStep", jobRepository)
                .<Voucher, FCMNotificationRequestDto>chunk(50, transactionManager)
                .reader(voucherExpirationItemReader())
                .processor(voucherExpirationItemProcessor())
                .writer(voucherExpirationItemWriter())
                .build();
    }
}
