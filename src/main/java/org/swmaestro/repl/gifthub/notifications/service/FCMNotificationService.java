package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
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
			} catch (FirebaseMessagingException e) {
				deviceTokenService.delete(deviceToken.getToken());
			}
		}
	}
}
