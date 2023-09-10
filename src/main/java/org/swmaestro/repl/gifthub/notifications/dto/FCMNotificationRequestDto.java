package org.swmaestro.repl.gifthub.notifications.dto;

import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FCMNotificationRequestDto {
	private Member targetMember;
	private Voucher targetVoucher;
	private String title;
	private String body;

	@Builder
	public FCMNotificationRequestDto(Member targetMember, Voucher targetVoucher, String title, String body) {
		this.targetMember = targetMember;
		this.targetVoucher = targetVoucher;
		this.title = title;
		this.body = body;
	}
}
