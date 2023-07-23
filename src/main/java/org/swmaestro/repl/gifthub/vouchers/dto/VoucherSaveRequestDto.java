package org.swmaestro.repl.gifthub.vouchers.dto;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoucherSaveRequestDto implements Serializable {
	private String barcode;
	private String expiresAt;
	private String productName;
	private String brandName;
	private String imageUrl;

	@Builder
	public VoucherSaveRequestDto(String barcode, String expiresAt, String productName, String brandName,
		String imageUrl) {
		this.barcode = barcode;
		this.expiresAt = expiresAt;
		this.productName = productName;
		this.brandName = brandName;
		this.imageUrl = imageUrl;
	}
}
