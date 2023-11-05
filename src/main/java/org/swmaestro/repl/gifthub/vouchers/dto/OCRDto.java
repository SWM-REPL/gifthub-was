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
public class OCRDto {
	private List<String> texts;

	@Builder
	public OCRDto(List<String> texts) {
		this.texts = texts;
	}

	public String concatenateTexts() {
		return String.join(";", texts);
	}
}