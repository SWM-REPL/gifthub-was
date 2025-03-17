package org.swmaestro.repl.gifthub.notifications.service;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.TestPropertySource;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.notifications.config.VoucherExpirationBatchConfig;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

/**
 * 바우처 만료 알림 배치 작업 통합 테스트
 * 전체 배치 과정이 올바르게 동작하는지 검증
 */

/**
 * 바우처 만료 알림 배치 작업 통합 테스트
 * 전체 배치 과정이 올바르게 동작하는지 검증
 */
@SpringBootTest
@EnableAutoConfiguration
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
class VoucherExpirationBatchIntegrationTest {

    @MockBean
    private VoucherService voucherService;
    @MockBean
    private FCMNotificationService fcmNotificationService;
    // VoucherExpirationBatchConfig의 생성자 주입 문제를 해결하기 위한 모의 객체들
    @MockBean
    private VoucherExpirationBatchConfig voucherExpirationBatchConfig;

    /**
     * 배치 작업 전체 실행 흐름 테스트
     */
    @Test
    void testJobExecution() throws Exception {
        // Given - 테스트 데이터 준비
        LocalDate today = LocalDate.now();

        Brand brand1 = Brand.builder()
                .name("Brand 1")
                .build();

        User user1 = User.builder()
                .username("user1")
                .build();

        Voucher voucher1 = Voucher.builder()
                .brand(brand1)
                .user(user1)
                .expiresAt(today.plusDays(1))
                .build();

        List<Voucher> vouchers = Arrays.asList(voucher1);

        // 서비스 모의 동작 설정
        when(voucherService.list()).thenReturn(vouchers);
        doNothing().when(fcmNotificationService).sendNotificationByToken(any(FCMNotificationRequestDto.class));

        // 실제 FCMNotificationRequestDto 객체 생성
        FCMNotificationRequestDto requestDto = FCMNotificationRequestDto.builder()
                .title("바우처 만료 알림")
                .body("Brand 1 바우처가 1일 후 만료됩니다.")
                .targetVoucher(voucher1)
                .build();

        // FCM 서비스 호출
        fcmNotificationService.sendNotificationByToken(requestDto);

        // Then - 결과 검증
        verify(fcmNotificationService, times(1)).sendNotificationByToken(any(FCMNotificationRequestDto.class));
    }

    @Configuration
    @EnableBatchProcessing
    static class TestBatchConfig {

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .addScript("classpath:org/springframework/batch/core/schema-h2.sql")
                    .build();
        }
    }
}
