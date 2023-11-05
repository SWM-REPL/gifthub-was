package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VoucherUseResponseDto {
	private Long usageId;
	private Long voucherId;
	private int balance;
	private Integer price;

	@Builder
	public VoucherUseResponseDto(Long usageId, Long voucherId, int balance, Integer price) {
		this.usageId = usageId;
		this.voucherId = voucherId;
		this.balance = balance;
		this.price = price;
	}
}