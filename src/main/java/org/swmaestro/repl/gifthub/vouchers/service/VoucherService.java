package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.util.ISO8601Converter;
import org.swmaestro.repl.gifthub.vouchers.dto.S3FileDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {
	@Value("${cloud.aws.s3.voucher-dir-name}")
	private String voucherDirName;
	private final StorageService storageService;
	private final BrandService brandService;
	private final ProductService productService;
	private final VoucherRepository voucherRepository;

	public Long save(VoucherDto voucherDto) throws IOException {
		S3FileDto s3FileDto = storageService.save(voucherDirName, voucherDto.getImageFile());
		Voucher voucher = Voucher.builder()
			.brand(brandService.read(voucherDto.getBrandName()))
			.product(productService.read(voucherDto.getProductName()))
			.barcode(voucherDto.getBarcode())
			.expiresAt(ISO8601Converter.iso8601ToLocalDateTime(voucherDto.getExpiresAt()))
			.imageUrl(s3FileDto.getUploadFileUrl())
			.build();

		return voucherRepository.save(voucher).getId();
	}

}
