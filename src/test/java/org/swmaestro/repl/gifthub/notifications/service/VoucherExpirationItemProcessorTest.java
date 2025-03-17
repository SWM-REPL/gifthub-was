package org.swmaestro.repl.gifthub.notifications.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

class VoucherExpirationItemProcessorTest {

    private VoucherExpirationItemProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new VoucherExpirationItemProcessor();
    }

    /**
     * 오늘 만료되는 바우처의 메시지 생성 테스트
     */
    @Test
    void shouldCreateTodayExpirationMessage() throws Exception {
        // Given - 테스트 데이터 준비
        LocalDate today = LocalDate.now();
        Brand brand = Brand.builder()
                .name("Test Brand")
                .build();

        User user = User.builder()
                .build();

        Voucher voucher = Voucher.builder()
                .expiresAt(today)
                .brand(brand)
                .user(user)
                .build();

        // When - 프로세서 실행
        FCMNotificationRequestDto result = processor.process(voucher);

        // Then - 결과 검증
        assertNotNull(result);
        assertEquals(user, result.getTargetUser());
        assertEquals(voucher, result.getTargetVoucher());
        assertEquals(NotificationType.EXPIRATION.getDescription(), result.getTitle());
        assertEquals("Test Brand에서 사용할 수 있는 기프티콘이 오늘 만료됩니다.", result.getBody());
    }

    /**
     * 미래에 만료되는 바우처의 메시지 생성 테스트
     */
    @Test
    void shouldCreateFutureDaysExpirationMessage() throws Exception {
        // Given - 테스트 데이터 준비
        LocalDate today = LocalDate.now();
        Brand brand = Brand.builder()
                .name("Test Brand")
                .build();

        User user = User.builder()
                .build();

        Voucher voucher = Voucher.builder()
                .expiresAt(today.plusDays(2))
                .brand(brand)
                .user(user)
                .build();

        // When - 프로세서 실행
        FCMNotificationRequestDto result = processor.process(voucher);

        // Then - 결과 검증
        assertNotNull(result);
        assertEquals("Test Brand에서 사용할 수 있는 기프티콘 기한이 2일 남았습니다.", result.getBody());
    }
}
