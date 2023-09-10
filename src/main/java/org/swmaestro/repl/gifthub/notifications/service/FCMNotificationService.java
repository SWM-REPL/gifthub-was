package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
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
	private final NotificationService notificationService;
	private final MemberService memberService;

	public void sendNotificationByToken(FCMNotificationRequestDto requestDto) {
		List<DeviceToken> deviceTokenList = deviceTokenService.list(requestDto.getTargetMember().getId());

		for (DeviceToken deviceToken : deviceTokenList) {
			System.out.println("deviceToken: " + deviceToken.getToken());

			Notification notification = Notification.builder()
					.setTitle(requestDto.getTitle())
					.setBody(requestDto.getBody())
					.build();

			org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
					= notificationService.save(requestDto.getTargetMember(), requestDto.getTargetVoucher(), NotificationType.EXPIRATION, requestDto.getBody());

			Message message = Message.builder()
					.setToken(deviceToken.getToken())
					.setNotification(notification)
					.putData("notification_id", savedNotification.getId().toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (Exception e) {
				throw new BusinessException("FCM 알림 전송에 실패했습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
			}
		}
	}
}
