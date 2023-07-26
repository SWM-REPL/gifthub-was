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
public class VoucherUseRequestDto {
	private Long id;
	private int amount;
	private String place;

	@Builder
	public VoucherUseRequestDto(Long id, int amount, String place) {
		this.id = id;
		this.amount = amount;
		this.place = place;
	}
}