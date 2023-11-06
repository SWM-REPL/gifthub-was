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
public class VoucherAutoSaveRequestDto {
	private List<String> texts;
	private String filename;

	@Builder
	public VoucherAutoSaveRequestDto(List<String> texts, String filename) {
		this.texts = texts;
		this.filename = filename;
	}

	public String concatenateTexts() {
		return String.join(";", texts);
	}
}