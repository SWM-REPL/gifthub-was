package org.swmaestro.repl.gifthub.notifications.service;

import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

class FCMNotificationItemWriterTest {

    @Mock
    private FCMNotificationService fcmNotificationService;

    private FCMNotificationItemWriter writer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        writer = new FCMNotificationItemWriter(fcmNotificationService);
    }

    /**
     * 각 항목마다 알림을 올바르게 전송하는지 테스트
     */
    @Test
    void shouldSendNotificationsForEachItem() throws Exception {
        // Given - 테스트 데이터 준비
        User user1 = User.builder()
                .build();
        Voucher voucher1 = Voucher.builder()
                .build();
        FCMNotificationRequestDto requestDto1 = FCMNotificationRequestDto.builder()
                .targetUser(user1)
                .targetVoucher(voucher1)
                .title(NotificationType.EXPIRATION.getDescription())
                .body("Message 1")
                .build();

        User user2 = User.builder()
                .build();
        Voucher voucher2 = Voucher.builder()
                .build();
        FCMNotificationRequestDto requestDto2 = FCMNotificationRequestDto.builder()
                .targetUser(user2)
                .targetVoucher(voucher2)
                .title(NotificationType.EXPIRATION.getDescription())
                .body("Message 2")
                .build();

        // 청크 생성
        Chunk<FCMNotificationRequestDto> chunk = new Chunk<>(Arrays.asList(requestDto1, requestDto2));

        // When - 작성기 실행
        writer.write(chunk);

        // Then - 결과 검증
        // 각 알림 요청이 FCM 서비스로 전달되었는지 확인
        verify(fcmNotificationService, times(1)).sendNotificationByToken(requestDto1);
        verify(fcmNotificationService, times(1)).sendNotificationByToken(requestDto2);
    }
}
