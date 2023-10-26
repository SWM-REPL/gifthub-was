package org.swmaestro.repl.gifthub.notifications.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.Device;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.DeviceService;
import org.swmaestro.repl.gifthub.auth.service.UserService;
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
	private final DeviceService deviceService;
	private final NotificationService notificationService;
	private final UserService userService;

	public void sendNotificationByToken(FCMNotificationRequestDto requestDto) {
		Long userId = requestDto.getTargetUser().getId();
		List<Device> deviceList = deviceService.list(userId);

		for (Device device : deviceList) {
			if (device.getFcmToken() == null) {
				continue;
			}
			Notification notification = Notification.builder()
					.setTitle(requestDto.getTitle())
					.setBody(requestDto.getBody())
					.build();

			org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
					= notificationService.save(requestDto.getTargetUser(), requestDto.getTargetVoucher(), NotificationType.EXPIRATION, requestDto.getBody());

			Message message = Message.builder()
					.setToken(device.getFcmToken())
					.setNotification(notification)
					.putData("notification_id", savedNotification.getId().toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
				deviceService.delete(userId, device.getDeviceToken());
			}
		}
	}

	/**
	 * 모든 회원에게 알림을 보내는 메서드
	 * @param noticeNotificationDto
	 */

	public void sendNotification(NoticeNotificationDto noticeNotificationDto) {
		List<Device> deviceList = deviceService.list();
		for (Device device : deviceList) {
			if (device.getFcmToken() == null) {
				continue;
			}
			Notification notification = Notification.builder()
					.setTitle(noticeNotificationDto.getTitle())
					.setBody(noticeNotificationDto.getBody())
					.build();
			Long userId = device.getUserId();
			org.swmaestro.repl.gifthub.notifications.entity.Notification savedNotification
					= notificationService.save(userService.readById(userId), null, NotificationType.NOTICE, noticeNotificationDto.getBody());

			Message message = Message.builder()
					.setToken(device.getFcmToken())
					.setNotification(notification)
					.putData("notification_id", savedNotification.getId().toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
				deviceService.delete(device.getId());
			}
		}
	}

	/**
	 * title과 body를 받아서 특정 회원에게 알림을 보내는 메서드(username으로 검색)
	 */
	public void sendNotification(String title, String body, String username) {
		User user = userService.read(username);

		NoticeNotificationDto noticeNotificationDto = NoticeNotificationDto.builder()
				.title(title)
				.body(body)
				.build();

		List<Device> deviceList = deviceService.list(user.getId());
		for (Device device : deviceList) {
			if (device.getFcmToken() == null) {
				continue;
			}
			Notification notification = Notification.builder()
					.setTitle(noticeNotificationDto.getTitle())
					.setBody(noticeNotificationDto.getBody())
					.build();

			Message message = Message.builder()
					.setToken(device.getFcmToken())
					.setNotification(notification)
					.putData("notification_type", NotificationType.REGISTERED.toString())
					.build();

			try {
				firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
				deviceService.delete(user.getId(), device.getDeviceToken());
			}
		}
	}
}
