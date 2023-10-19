package org.swmaestro.repl.gifthub.giftcard.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GiftcardResponseDto {
	private String sender;
	private String message;
	private String brandName;
	private String productName;
	private LocalDate expiresAt;

	@Builder
	public GiftcardResponseDto(String sender, String message, String brandName, String productName,
			LocalDate expiresAt) {
		this.sender = sender;
		this.message = message;
		this.brandName = brandName;
		this.productName = productName;
		this.expiresAt = expiresAt;
	}
}
