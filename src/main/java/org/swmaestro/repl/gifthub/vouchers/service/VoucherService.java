package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.ErrorCode;
import org.swmaestro.repl.gifthub.util.ISO8601Converter;
import org.swmaestro.repl.gifthub.vouchers.dto.S3FileDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
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
	private final MemberService memberService;

	/*
		기프티콘 저장 메서드
	 */
	public VoucherSaveResponseDto save(VoucherDto voucherDto) throws IOException {
		S3FileDto s3FileDto = storageService.save(voucherDirName, voucherDto.getImageFile());
		Voucher voucher = Voucher.builder()
			.brand(brandService.read(voucherDto.getBrandName()))
			.product(productService.read(voucherDto.getProductName()))
			.barcode(voucherDto.getBarcode())
			.expiresAt(ISO8601Converter.iso8601ToLocalDateTime(voucherDto.getExpiresAt()))
			.imageUrl(s3FileDto.getUploadFileUrl())
			.member(memberService.read(voucherDto.getUsername()))
			.build();

		return VoucherSaveResponseDto.builder()
			.id(voucherRepository.save(voucher).getId())
			.build();
	}

	/*
	기프티콘 상세 조회 메서드
	 */
	public Optional<Voucher> read(Long id) {
		Optional<Voucher> voucher = voucherRepository.findById(id);

		if (voucher == null) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", ErrorCode.NOT_FOUND_RESOURCE);
		}
		return voucher;
	}

	/*
	사용자 별 기프티콘 목록 조회 메서드
	 */
	public List<Voucher> list(String username) {
		List<Voucher> vouchers = voucherRepository.findByMemberUsername(username);
		if (vouchers == null) {
			throw new BusinessException("존재하지 않는 사용자 입니다.", ErrorCode.NOT_FOUND_RESOURCE);
		}
		return vouchers;
	}

	/*
	사용자, 브랜드 별 기프티콘 목록 조회 메서드
	 */
	public List<Voucher> listByBrand(String username, String brandName) {
		List<Voucher> vouchers = voucherRepository.findByMemberUsernameAndBrandName(username, brandName);
		if (vouchers == null) {
			throw new BusinessException("존재하지 않는 사용자 입니다.", ErrorCode.NOT_FOUND_RESOURCE);
		}
		return vouchers;
	}
}
