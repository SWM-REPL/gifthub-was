package org.swmaestro.repl.gifthub.vouchers.dto;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoucherSaveResponseDto implements Serializable {
	private Long id;

	@Builder
	public VoucherSaveResponseDto(Long id) {
		this.id = id;
	}
}


