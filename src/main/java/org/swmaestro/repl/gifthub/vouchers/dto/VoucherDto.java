package org.swmaestro.repl.gifthub.vouchers.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class VoucherDto {
	private String barcode;
	private String expiresAt;
	private String productName;
	private String brandName;
	private MultipartFile imageFile;
}
