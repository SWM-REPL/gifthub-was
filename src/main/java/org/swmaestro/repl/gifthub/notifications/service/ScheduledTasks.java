package org.swmaestro.repl.gifthub.notifications.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.service.DeviceTokenService;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.notifications.dto.FCMNotificationRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {
	private final VoucherService voucherService;
	private final DeviceTokenService deviceTokenService;
	private final FCMNotificationService fcmNotificationService;

	@Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul") // 매일 오전 10시 실행
	public void sendExpirationNotification() {
		Date now = new Date();
		LocalDate today = LocalDate.of(now.getYear(), now.getMonth(), now.getDay());

		List<Voucher> voucherList = voucherService.list();

		for (Voucher voucher : voucherList) {
			long daysDifference = ChronoUnit.DAYS.between(today, voucher.getExpiresAt());

			if (daysDifference <= 3) {
				FCMNotificationRequestDto requestDto = FCMNotificationRequestDto.builder()
						.targetMemberId(voucher.getMember().getId())
						.title(NotificationType.EXPIRATION.getDescription())
						.body(voucher.getBrand().getName() + "에서 사용할 수 있는 기프티콘 기한이 " + daysDifference + "일 남았습니다.")
						.build();

				fcmNotificationService.sendNotificationByToken(requestDto);
			}
		}
	}

	@Scheduled(cron = "0 0 10 * * ?", zone = "Asia/Seoul") // 매일 오전 9시 실행
	public void deleteDeviceToken() {
		LocalDateTime now = LocalDateTime.now();

		List<DeviceToken> DeviceTokenList = de.findAll();

		for (Voucher voucher : voucherList) {
			long daysDifference = ChronoUnit.DAYS.between(today, voucher.getExpiresAt());

			if (daysDifference <= 3) {
				FCMNotificationRequestDto requestDto = FCMNotificationRequestDto.builder()
						.targetMemberId(voucher.getMember().getId())
						.title(NotificationType.EXPIRATION.getDescription())
						.body(voucher.getBrand().getName() + "에서 사용할 수 있는 기프티콘 기한이 " + daysDifference + "일 남았습니다.")
						.build();

				fcmNotificationService.sendNotificationByToken(requestDto);
			}
		}
	}
}
