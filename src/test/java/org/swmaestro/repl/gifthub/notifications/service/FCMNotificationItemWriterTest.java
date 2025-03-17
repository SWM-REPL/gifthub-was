package org.swmaestro.repl.gifthub.notifications.service;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.Chunk;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;

class FCMNotificationItemWriterTest {

    @Mock
    private FCMNotificationService fcmNotificationService;

    @Mock
    private FCMNotificationItemWriter fcmNotificationItemWriter;

    private RetryTemplate createRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 재시도 정책 설정 (3회 시도)
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);

        // 백오프 정책 설정 (초기 1초, 배수 2의 지수 백오프)
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fcmNotificationItemWriter = new FCMNotificationItemWriter(fcmNotificationService, createRetryTemplate());
    }

    /**
     * 각 항목마다 알림을 올바르게 전송하는지 테스트
     */
    @Test
    void shouldSendNotificationsForEachItem() throws Exception {
        // given
        List<FCMNotificationRequestDto> requestDtos = new ArrayList<>();
        FCMNotificationRequestDto dto1 = new FCMNotificationRequestDto();
        FCMNotificationRequestDto dto2 = new FCMNotificationRequestDto();
        requestDtos.add(dto1);
        requestDtos.add(dto2);
        Chunk<FCMNotificationRequestDto> chunk = new Chunk<>(requestDtos);

        // when
        fcmNotificationItemWriter.write(chunk);

        // then
        // 개별 호출 대신 배치 호출을 검증
        verify(fcmNotificationService, times(1)).sendBatchNotifications(requestDtos);
    }
}
