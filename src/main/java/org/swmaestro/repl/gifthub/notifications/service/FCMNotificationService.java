package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.notifications.dto.NoticeNotificationDto;

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
	private final NotificationService notificationService;
	private final MemberService memberService;

	public void sendNotificationByToken(FCMNotificationRequestDto requestDto) {
		List<DeviceToken> deviceTokenList = deviceTokenService.list(requestDto.getTargetMember().getId());

		for (DeviceToken deviceToken : deviceTokenList) {
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
			} catch (FirebaseMessagingException e) {
				deviceTokenService.delete(deviceToken.getToken());
			}
		}
	}

	/**
	 * 모든 회원에게 알림을 보내는 메서드
	 * @param noticeNotificationDto
	 */

	public void sendNotification(NoticeNotificationDto noticeNotificationDto) {
		List<DeviceToken> deviceTokenList = deviceTokenService.list();

		for (DeviceToken deviceToken : deviceTokenList) {
			Notification notification = Notification.builder()
					.setTitle(noticeNotificationDto.getTitle())
					.setBody(noticeNotificationDto.getBody())
					.build();

			org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
					= notificationService.save(deviceToken.getMember(), null, NotificationType.NOTICE, noticeNotificationDto.getBody());

			Message message = Message.builder()
					.setToken(deviceToken.getToken())
					.setNotification(notification)
					.putData("notification_id", savedNotification.getId().toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
				deviceTokenService.delete(deviceToken.getToken());
			}
		}
	}

	/**
	 * title과 body를 받아서 특정 회원에게 알림을 보내는 메서드(username으로 검색)
	 */
	public void sendNotification(String title, String body, String username) {
		Member member = memberService.read(username);

		NoticeNotificationDto noticeNotificationDto = NoticeNotificationDto.builder()
				.title(title)
				.body(body)
				.build();

		List<DeviceToken> deviceTokenList = deviceTokenService.list(member.getId());

		for (DeviceToken deviceToken : deviceTokenList) {
			Notification notification = Notification.builder()
					.setTitle(noticeNotificationDto.getTitle())
					.setBody(noticeNotificationDto.getBody())
					.build();

			Message message = Message.builder()
					.setToken(deviceToken.getToken())
					.setNotification(notification)
					.putData("notification_type", NotificationType.REGISTERED.toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
				deviceTokenService.delete(deviceToken.getToken());
			}
		}
	}
}
