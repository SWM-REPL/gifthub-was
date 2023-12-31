package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class VoucherUpdateResponseDto {
	private Long id;
	private Long productId;
	private String barcode;
	private String expiresAt;
	private Integer price;
	private Integer balance;
	private String imageUrl;
	@JsonProperty("is_accessible")
	private boolean accessible;
	@JsonProperty("is_shared")
	private boolean shared;
	@JsonProperty("is_checked")
	private boolean checked;

	@Builder
	public VoucherUpdateResponseDto(Long id, Long productId, String barcode, String expiresAt, Integer price,
			Integer balance, String imageUrl, boolean accessible, boolean shared, boolean checked) {
		this.id = id;
		this.productId = productId;
		this.barcode = barcode;
		this.expiresAt = expiresAt;
		this.price = price;
		this.balance = balance;
		this.imageUrl = imageUrl;
		this.accessible = accessible;
		this.shared = shared;
		this.checked = checked;
	}
}
