package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FCMNotificationService {
	private final FirebaseMessaging firebaseMessaging;
	private final DeviceTokenService deviceTokenService;

	public void sendNotificationByToken(FCMNotificationRequestDto requestDto) {
		List<DeviceToken> deviceTokenList = deviceTokenService.list(requestDto.getTargetMemberId());

		for (DeviceToken deviceToken : deviceTokenList) {
			Notification notification = Notification.builder()
					.setTitle(requestDto.getTitle())
					.setBody(requestDto.getBody())
					.build();

			Message message = Message.builder()
					.setToken(deviceToken.getToken())
					.setNotification(notification)
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (Exception e) {
				throw new BusinessException("FCM 알림 전송에 실패했습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
