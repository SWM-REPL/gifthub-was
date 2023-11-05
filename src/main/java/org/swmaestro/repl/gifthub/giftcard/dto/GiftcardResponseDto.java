package org.swmaestro.repl.gifthub.giftcard.dto;

import java.time.LocalDate;

import org.swmaestro.repl.gifthub.vouchers.dto.BrandReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.ProductReadResponseDto;

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
	private BrandReadResponseDto brand;
	private ProductReadResponseDto product;
	private LocalDate expiresAt;

	@Builder
	public GiftcardResponseDto(String sender, String message, BrandReadResponseDto brand, ProductReadResponseDto product, LocalDate expiresAt) {
		this.sender = sender;
		this.message = message;
		this.brand = brand;
		this.product = product;
		this.expiresAt = expiresAt;
	}
}
