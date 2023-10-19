package org.swmaestro.repl.gifthub.giftcard.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GiftcardRequestDto {
	private String password;

	@Builder
	public GiftcardRequestDto(String password) {
		this.password = password;
	}
}
