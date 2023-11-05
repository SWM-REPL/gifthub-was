package org.swmaestro.repl.gifthub.notifications.dto;

import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FCMNotificationRequestDto {
	private User targetUser;
	private Voucher targetVoucher;
	private String title;
	private String body;

	@Builder
	public FCMNotificationRequestDto(User targetUser, Voucher targetVoucher, String title, String body) {
		this.targetUser = targetUser;
		this.targetVoucher = targetVoucher;
		this.title = title;
		this.body = body;
	}
}
