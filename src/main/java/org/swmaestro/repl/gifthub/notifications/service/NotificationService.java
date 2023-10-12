package org.swmaestro.repl.gifthub.notifications.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.NotificationReadResponseDto;
import org.swmaestro.repl.gifthub.notifications.entity.Notification;
import org.swmaestro.repl.gifthub.notifications.repository.NotificationRepository;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final MemberService memberService;
	private final NotificationRepository notificationRepository;
	private final DeviceTokenService deviceTokenService;
	private final VoucherService voucherService;

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
				.type(notification.getType().getDescription())
				.message(notification.getMessage())
				.notifiedAt(notification.getCreatedAt())
				.voucherId(notification.getVoucher().getId())
				.checkedAt(notification.getCheckedAt())
				.build();
		return notificationReadResponseDto;
	}

	/**
	 * 디바이스 토큰을 저장하는 메서드
	 */
	public boolean saveDeviceToken(String username, String deviceToken) {
		try {
			deviceTokenService.save(username, deviceToken);
			return true;
		} catch (Exception e) {
			throw new BusinessException("디바이스 토큰 저장에 실패하였습니다.", StatusEnum.BAD_REQUEST);
		}

	}

	public boolean deleteDeviceToken(Member member, String deviceToken) {
		try {
			deviceTokenService.delete(member, deviceToken);
			return true;
		} catch (Exception e) {
			throw new BusinessException("디바이스 토큰 삭제에 실패하였습니다.", StatusEnum.BAD_REQUEST);
		}
	}

	/**
	 * Notification 저장 메서드
	 */
	public Notification save(Member member, Voucher voucher, NotificationType type, String message) {
		Notification notification = Notification.builder()
				.receiver(member)
				.type(type)
				.message(message)
				.voucher(voucher)
				// .createdAt(LocalDateTime.now())
				.build();
		return notificationRepository.save(notification);
	}

	/**
	 * Notification 상세 조회 메서드
	 */
	public NotificationReadResponseDto read(Long id, String username) {
		if (memberService.read(username) == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		Notification notification = notificationRepository.findById(id).orElseThrow(() -> new BusinessException("존재하지 않는 알림입니다.", StatusEnum.NOT_FOUND));

		if (!notification.getReceiver().getUsername().equals(username)) {
			throw new BusinessException("알림을 조회할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		NotificationReadResponseDto notificationReadResponseDto = mapToDto(notification);
		notification.setCheckedAt(LocalDateTime.now());
		notificationRepository.save(notification);
		return notificationReadResponseDto;
	}
}
