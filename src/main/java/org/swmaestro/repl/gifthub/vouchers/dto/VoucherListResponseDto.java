package org.swmaestro.repl.gifthub.vouchers.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VoucherListResponseDto {
	private List<Long> voucherIds;
	private int pendingCount;

	@Builder
	public VoucherListResponseDto(List<Long> voucherIds, int pendingCount) {
		this.voucherIds = voucherIds;
		this.pendingCount = pendingCount;
	}
}
