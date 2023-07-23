package org.swmaestro.repl.gifthub.vouchers.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.ErrorCode;
import org.swmaestro.repl.gifthub.util.DateConverter;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherService {
	private final StorageService storageService;
	private final BrandService brandService;
	private final ProductService productService;
	private final VoucherRepository voucherRepository;
	private final MemberService memberService;

	/*
		기프티콘 저장 메서드
	 */
	public VoucherSaveResponseDto save(String username, VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		Voucher voucher = Voucher.builder()
				.brand(brandService.read(voucherSaveRequestDto.getBrandName()))
				.product(productService.read(voucherSaveRequestDto.getProductName()))
				.barcode(voucherSaveRequestDto.getBarcode())
				.expiresAt(DateConverter.stringToLocalDate(voucherSaveRequestDto.getExpiresAt()))
				.imageUrl(voucherSaveRequestDto.getImageUrl())
				.member(memberService.read(username))
				.build();

		return VoucherSaveResponseDto.builder()
				.id(voucherRepository.save(voucher).getId())
				.build();
	}

	/*
	기프티콘 상세 조회 메서드
	 */
	public VoucherReadResponseDto read(Long id) {
		Optional<Voucher> voucher = voucherRepository.findById(id);

		if (voucher == null) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", ErrorCode.NOT_FOUND_RESOURCE);
		}
		VoucherReadResponseDto voucherReadResponseDto = VoucherReadResponseDto.builder()
				.id(voucher.get().getId())
				.barcode(voucher.get().getBarcode())
				.expiresAt(voucher.get().getExpiresAt().toString())
				.product(voucher.get().getProduct())
				.username(voucher.get().getMember().getUsername())
				.build();
		return voucherReadResponseDto;
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
