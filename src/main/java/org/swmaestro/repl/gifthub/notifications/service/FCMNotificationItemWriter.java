package org.swmaestro.repl.gifthub.notifications.service;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;

import lombok.RequiredArgsConstructor;

/**
 * FCMNotificationRequestDto를 받아서 FCMNotificationService를 통해 FCM 메시지를 전송하는 ItemWriter
 */
@Component
@RequiredArgsConstructor
public class FCMNotificationItemWriter implements ItemWriter<FCMNotificationRequestDto> {
    private final FCMNotificationService fcmNotificationService;

    /**
     * FCMNotificationRequestDto를 받아서 FCMNotificationService를 통해 FCM 메시지를 전송한다.
     * @param chunk FCMNotificationRequestDto 리스트
     * @throws Exception
     */
    @Override
    public void write(Chunk<? extends FCMNotificationRequestDto> chunk) throws Exception {
        for (FCMNotificationRequestDto requestDto : chunk) {
            fcmNotificationService.sendNotificationByToken(requestDto);
        }
    }
}

