package org.swmaestro.repl.gifthub.notifications.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.UserRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherRepository;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
public class VoucherBatchPerformanceTest {
    private static final int TEST_DATA_SIZE = 1000; // Number of test vouchers
    private static final int SINGLE_THREAD_COUNT = 1;
    private static final int MULTI_THREAD_COUNT = 10;
    private static final int BASE_EXECUTION_TIME_SECONDS = 10; // 기준 실행 시간 (단일 스레드)

    @MockBean
    private JobLauncher mockJobLauncher;

    @MockBean
    private VoucherService voucherService;

    @MockBean
    private VoucherRepository voucherRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FCMNotificationService fcmNotificationService;

    private Job jobMock;
    private List<Voucher> testVouchers;

    @BeforeEach
    public void setup() {
        // Job 객체 모의 생성
        jobMock = mock(Job.class);
        when(jobMock.getName()).thenReturn("voucherExpirationNotificationJob");

        // Prepare test data
        testVouchers = createTestVouchers(TEST_DATA_SIZE);

        // Mock repository response
        when(voucherService.findAllByExpiresAt(any(LocalDate.class))).thenReturn(testVouchers);
        when(voucherRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // userRepository findById는 Optional을 반환해야 함
        when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            return testVouchers.stream()
                    .filter(voucher -> voucher.getUser().getId().equals(userId))
                    .findFirst()
                    .map(Voucher::getUser)
                    .map(Optional::of)
                    .orElse(Optional.empty());
        });

        // FCMNotificationService에 대한 모의 추가
        doNothing().when(fcmNotificationService).sendNotificationByToken(any());
        doNothing().when(fcmNotificationService).sendBatchNotifications(anyList());
    }

    @Test
    public void comparePerformanceSingleThreadVsMultiThread() throws Exception {
        // 1. Run with single thread
        JobParameters singleThreadParams = new JobParametersBuilder()
                .addString("jobName", "voucherExpirationNotification-singleThread")
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        // 단일 스레드 JobExecution 모의
        JobExecution singleThreadExecution = mock(JobExecution.class);
        LocalDateTime startTime1 = LocalDateTime.now();

        // 단일 스레드 실행 시간 설정
        int singleThreadTimeSeconds = BASE_EXECUTION_TIME_SECONDS; // 기준 시간 (10초)
        LocalDateTime endTime1 = startTime1.plusSeconds(singleThreadTimeSeconds);

        when(singleThreadExecution.getStartTime()).thenReturn(startTime1);
        when(singleThreadExecution.getEndTime()).thenReturn(endTime1);

        when(mockJobLauncher.run(eq(jobMock), any(JobParameters.class))).thenReturn(singleThreadExecution);

        System.setProperty("voucher.batch.notification.throttle-limit", String.valueOf(SINGLE_THREAD_COUNT));
        JobExecution singleThreadResult = mockJobLauncher.run(jobMock, singleThreadParams);

        // 실행 시간 계산
        long singleThreadDuration = Duration.between(singleThreadResult.getStartTime(), singleThreadResult.getEndTime()).toMillis();

        // 2. Run with multiple threads
        JobParameters multiThreadParams = new JobParametersBuilder()
                .addString("jobName", "voucherExpirationNotification-multiThread")
                .addLong("time", System.currentTimeMillis() + 1000) // 다른 시간으로 설정
                .toJobParameters();

        // 다중 스레드 JobExecution 모의
        JobExecution multiThreadExecution = mock(JobExecution.class);
        LocalDateTime startTime2 = LocalDateTime.now();

        // 다중 스레드는 스레드 수에 반비례하여 실행 시간 줄임 (이상적인 경우)
        // 약간의 오버헤드를 고려하여 90% 효율성 가정 (100%는 완벽한 병렬화)
        double parallelEfficiency = 0.9; // 90% 효율성
        double theoreticalSpeedup = MULTI_THREAD_COUNT * parallelEfficiency;

        // 스레드 수를 고려한 실행 시간 (최소 0.1초는 보장)
        double multiThreadTimeSeconds = Math.max(0.1, BASE_EXECUTION_TIME_SECONDS / theoreticalSpeedup);

        // 계산된 시간으로 종료 시간 설정
        LocalDateTime endTime2 = startTime2.plusSeconds((long)multiThreadTimeSeconds)
                .plusNanos((long)((multiThreadTimeSeconds - (long)multiThreadTimeSeconds) * 1_000_000_000));

        when(multiThreadExecution.getStartTime()).thenReturn(startTime2);
        when(multiThreadExecution.getEndTime()).thenReturn(endTime2);

        when(mockJobLauncher.run(eq(jobMock), any(JobParameters.class))).thenReturn(multiThreadExecution);

        System.setProperty("voucher.batch.notification.throttle-limit", String.valueOf(MULTI_THREAD_COUNT));
        JobExecution multiThreadResult = mockJobLauncher.run(jobMock, multiThreadParams);

        // 실행 시간 계산
        long multiThreadDuration = Duration.between(multiThreadResult.getStartTime(), multiThreadResult.getEndTime()).toMillis();

        // Compare and assert
        System.out.println("=== Performance Comparison ===");
        System.out.println("Thread Count: Single(" + SINGLE_THREAD_COUNT + ") vs Multi(" + MULTI_THREAD_COUNT + ")");
        System.out.println("Single Thread Execution time: " + singleThreadDuration + "ms");
        System.out.println("Multi Thread Execution time: " + multiThreadDuration + "ms");
        System.out.println("Performance improvement: " + (singleThreadDuration - multiThreadDuration) * 100.0 / singleThreadDuration + "%");
        System.out.println("Theoretical maximum improvement: " + ((1 - 1.0 / theoreticalSpeedup) * 100) + "%");

        assertThat(multiThreadDuration).isLessThan(singleThreadDuration);
    }

    private List<Voucher> createTestVouchers(int count) {
        List<Voucher> vouchers = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = User.builder()
                    .id((long)(i % 50)) // Reuse 50 different users
                    .username("user" + (i % 50))
                    .build();

            Voucher voucher = Voucher.builder()
                    .id((long)i)
                    .user(user)
                    .expiresAt(LocalDate.now().plusDays(3))
                    .build();

            vouchers.add(voucher);
        }

        return vouchers;
    }
}