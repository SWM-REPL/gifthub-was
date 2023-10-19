package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VoucherShareRequestDto {
	private String message;

	@Builder
	public VoucherShareRequestDto(String message) {
		this.message = message;
	}
}
