package org.swmaestro.repl.gifthub.notifications.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;

import lombok.RequiredArgsConstructor;

/**
 * FCMNotificationRequestDto를 받아서 FCMNotificationService를 통해 FCM 메시지를 전송하는 ItemWriter
 */
@Component
@RequiredArgsConstructor
public class FCMNotificationItemWriter implements ItemWriter<FCMNotificationRequestDto> {
    private static final Logger logger = LoggerFactory.getLogger(FCMNotificationItemWriter.class);

    private final FCMNotificationService fcmNotificationService;
    private final RetryTemplate retryTemplate;

    @Override
    public void write(Chunk<? extends FCMNotificationRequestDto> notificationRequests) throws Exception {
        if (notificationRequests.isEmpty()) {
            return;
        }

        logger.info("FCM 알림 전송 처리 중: {} 건", notificationRequests.size());

        try {
            retryTemplate.execute(new RetryCallback<Void, Exception>() {
                @Override
                public Void doWithRetry(RetryContext context) throws Exception {
                    // 재시도 시도인 경우 로그 기록
                    if (context.getRetryCount() > 0) {
                        logger.info("알림 전송 시도 {} 번째 재시도", context.getRetryCount());
                    }

                    // FCM 서비스를 통해 알림 전송
                    fcmNotificationService.sendBatchNotifications(notificationRequests.getItems());

                    return null;
                }
            });

            logger.info("FCM 알림 전송 완료: {} 건", notificationRequests.size());

        } catch (Exception e) {
            logger.error("재시도 후에도 FCM 알림 전송 실패: {}", e.getMessage());
            throw e; // Spring Batch가 실패를 처리하도록 예외 다시 던지기
        }
    }
}

