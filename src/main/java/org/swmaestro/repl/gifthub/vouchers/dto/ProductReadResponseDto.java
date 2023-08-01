package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductReadResponseDto {
	private Long id;
	private Long brand_id;
	private String name;
	private String description;
	private int isReusable;
	private int price;
	private String imageUrl;

	@Builder
	public ProductReadResponseDto(Long id, Long brand_id, String name, String description, int isReusable, int price, String imageUrl) {
		this.id = id;
		this.brand_id = brand_id;
		this.name = name;
		this.description = description;
		this.isReusable = isReusable;
		this.price = price;
		this.imageUrl = imageUrl;
	}
}
