package org.swmaestro.repl.gifthub.vouchers.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;

@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class VoucherReadResponseDto {
	private Long id;
	private String barcode;
	private String expiresAt;
	private Product product;
	private String username;
}
