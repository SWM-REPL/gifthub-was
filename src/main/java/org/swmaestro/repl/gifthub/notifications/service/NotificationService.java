package org.swmaestro.repl.gifthub.notifications.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.dto.NotificationReadResponseDto;
import org.swmaestro.repl.gifthub.notifications.entity.Notification;
import org.swmaestro.repl.gifthub.notifications.repository.NotificationRepository;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final MemberService memberService;
	private final NotificationRepository notificationRepository;
	private final DeviceTokenService deviceTokenService;

	public List<NotificationReadResponseDto> list(String username) {
		if (memberService.read(username) == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		List<Notification> notifications = notificationRepository.findAllByReceiverUsername(username);

		List<NotificationReadResponseDto> notificationReadResponseDtos = new ArrayList<>();

		for (Notification notification : notifications) {
			notificationReadResponseDtos.add(mapToDto(notification));
		}
		return notificationReadResponseDtos;
	}

	/*
	Entity를 Dto로 변환하는 메서드
	 */
	public NotificationReadResponseDto mapToDto(Notification notification) {
		NotificationReadResponseDto notificationReadResponseDto = NotificationReadResponseDto.builder()
				.id(notification.getId())
				.type(notification.getType())
				.message(notification.getMessage())
				.notifiedAt(notification.getCreatedAt())
				.voucherId(notification.getVoucher().getId())
				.build();
		return notificationReadResponseDto;
	}

	/**
	 * 디바이스 토큰을 저장하는 메서드
	 */
	public void saveDeviceToken(String username, String deviceToken) {
		try {
			deviceTokenService.save(username, deviceToken);
		} catch (Exception e) {
			throw new BusinessException("디바이스 토큰 저장에 실패하였습니다.", StatusEnum.BAD_REQUEST);
		}

	}
}
