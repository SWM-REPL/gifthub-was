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
public class VoucherShareResponseDto {
	private String id;

	@Builder
	public VoucherShareResponseDto(String id) {
		this.id = id;
	}
}
