package org.swmaestro.repl.gifthub.giftcard.dto;

import java.time.LocalDate;

import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;

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
	private Brand brand;
	private Product product;
	private LocalDate expiresAt;

	@Builder
	public GiftcardResponseDto(String sender, String message, Brand brand, Product product, LocalDate expiresAt) {
		this.sender = sender;
		this.message = message;
		this.brand = brand;
		this.product = product;
		this.expiresAt = expiresAt;
	}
}
