package org.swmaestro.repl.gifthub.vouchers.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

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
public class VoucherDto implements Serializable {
	private String barcode;
	private String expiresAt;
	private String productName;
	private String brandName;
	private MultipartFile imageFile;
	private String username;

	@Builder
	public VoucherDto(String barcode, String expiresAt, String productName, String brandName, MultipartFile imageFile,
		String username) {
		this.barcode = barcode;
		this.expiresAt = expiresAt;
		this.productName = productName;
		this.brandName = brandName;
		this.imageFile = imageFile;
		this.username = username;
	}
}
