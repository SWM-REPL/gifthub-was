package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.DateConverter;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.entity.VoucherUsageHistory;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherRepository;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherUsageHistoryRepository;

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
	private final VoucherUsageHistoryRepository voucherUsageHistoryRepository;

	/*
		기프티콘 저장 메서드
	 */
	public VoucherSaveResponseDto save(String username, VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		Brand brand = brandService.read(voucherSaveRequestDto.getBrandName()).orElseThrow(() ->
				new BusinessException("존재하지 않는 브랜드입니다.", StatusEnum.NOT_FOUND));
		Product product = productService.read(brand.getId(), voucherSaveRequestDto.getProductName()).orElseThrow(() ->
				new BusinessException("존재하지 않는 상품입니다.", StatusEnum.NOT_FOUND));
		String imageUrl = voucherSaveRequestDto.getImageUrl();

		if (isDuplicateVoucher(username, voucherSaveRequestDto.getBarcode()) == true) {
			throw new BusinessException("이미 등록된 기프티콘입니다.", StatusEnum.CONFLICT);
		}

		if (imageUrl != null) {
			imageUrl = storageService.getBucketAddress(voucherDirName) + voucherSaveRequestDto.getImageUrl();
		}

		Voucher voucher = Voucher.builder()
				.brand(brand)
				.product(product)
				.barcode(voucherSaveRequestDto.getBarcode())
				.expiresAt(DateConverter.stringToLocalDate(voucherSaveRequestDto.getExpiresAt()))
				.imageUrl(imageUrl)
				.balance(product.getPrice())
				.member(memberService.read(username))
				.build();

		return VoucherSaveResponseDto.builder()
				.id(voucherRepository.save(voucher).getId())
				.build();
	}

	/**
	 * 기프티콘 상세 조회 메서드
	 */
	public Voucher read(Long id) {
		return voucherRepository.findById(id)
				.orElseThrow(() -> new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND));
	}

	/*
	기프티콘 상세 조회 메서드
	 */
	public VoucherReadResponseDto read(Long id, String username) {
		Optional<Voucher> voucher = voucherRepository.findById(id);
		List<Voucher> vouchers = voucherRepository.findAllByMemberUsername(username);

		if (voucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}
		if (!vouchers.contains(voucher.get())) {
			throw new BusinessException("상품권을 조회할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		VoucherReadResponseDto voucherReadResponseDto = mapToDto(voucher.get());
		return voucherReadResponseDto;
	}

	/**
	 사용자 별 기프티콘 목록 조회 메서드
	 */
	public List<Voucher> list() {
		return voucherRepository.findAll();
	}

	/*
	사용자 별 기프티콘 목록 조회 메서드(userId로 조회, username으로 권한 대조)
	 */
	public List<Long> list(Long userId, String username) {
		if (!memberService.read(userId).getUsername().equals(username)) {
			throw new BusinessException("상품권을 조회할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		List<Voucher> vouchers = voucherRepository.findAllByMemberId(userId);
		List<Long> voucherIdList = new ArrayList<>();
		for (Voucher voucher : vouchers) {
			// 삭제된 기프티콘은 조회되지 않도록 함
			if (voucher.getDeletedAt() != null) {
				continue;
			}
			voucherIdList.add(voucher.getId());
		}
		return voucherIdList;
	}

	/*
	사용자 별 기프티콘 목록 조회 메서드(username으로 조회)
	 */
	public List<Long> list(String username) {
		List<Voucher> vouchers = voucherRepository.findAllByMemberUsername(username);
		List<Long> voucherIdList = new ArrayList<>();
		for (Voucher voucher : vouchers) {
			voucherIdList.add(voucher.getId());
		}
		return voucherIdList;
	}

	/*
	기프티콘 정보 수정 메서드
	 */
	public VoucherSaveResponseDto update(Long voucherId, VoucherUpdateRequestDto voucherUpdateRequestDto) {
		Voucher voucher = voucherRepository.findById(voucherId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND));

		if ((voucherUpdateRequestDto.getBalance() != null) && (voucherUpdateRequestDto.getBalance() > productService.read(voucher.getProduct().getName())
				.getPrice())) {
			throw new BusinessException("잔액은 상품 가격보다 클 수 없습니다.", StatusEnum.BAD_REQUEST);
		}

		voucher.setBarcode(
				voucherUpdateRequestDto.getBarcode() == null ? voucher.getBarcode() :
						voucherUpdateRequestDto.getBarcode());
		voucher.setBrand(voucherUpdateRequestDto.getBrandName() == null ? voucher.getBrand() :
				brandService.read(voucherUpdateRequestDto.getBrandName()).get());
		voucher.setProduct(voucherUpdateRequestDto.getProductName() == null ? voucher.getProduct() :
				productService.read(voucherUpdateRequestDto.getProductName()));

		voucher.setExpiresAt(voucherUpdateRequestDto.getExpiresAt() == null ? voucher.getExpiresAt() :
				DateConverter.stringToLocalDate(voucherUpdateRequestDto.getExpiresAt()));

		voucher.setBalance(voucherUpdateRequestDto.getBalance() == null ? voucher.getBalance() :
				voucherUpdateRequestDto.getBalance());

		voucherRepository.save(voucher);

		return VoucherSaveResponseDto.builder()
				.id(voucherId)
				.build();
	}

	/*
	기프티콘 사용 등록 메서드
	 */
	public VoucherUseResponseDto use(String username, Long voucherId, VoucherUseRequestDto voucherUseRequestDto) {
		if (voucherUseRequestDto.getAmount() == null || voucherUseRequestDto.getAmount() <= 0) {
			throw new BusinessException("사용 금액을 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
		if (voucherUseRequestDto.getPlace() == null) {
			throw new BusinessException("사용처를 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
		Optional<Voucher> voucher = voucherRepository.findById(voucherId);
		List<Voucher> vouchers = voucherRepository.findAllByMemberUsername(username);
		List<VoucherUsageHistory> voucherUsageHistories = voucherUsageHistoryRepository.findAllByVoucherId(voucherId);

		if (voucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}
		if (!vouchers.contains(voucher.get())) {
			throw new BusinessException("상품권을 사용할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		if (voucher.get().getBalance() == 0) {
			throw new BusinessException("이미 사용된 상품권 입니다.", StatusEnum.NOT_FOUND);
		}

		int remainingBalance = voucher.get().getBalance();
		int requestedAmount = voucherUseRequestDto.getAmount();

		if (requestedAmount > remainingBalance) {
			throw new BusinessException("잔액이 부족합니다.", StatusEnum.CONFLICT);
		}

		if (voucher.get().getExpiresAt().isBefore(LocalDate.now())) {
			throw new BusinessException("유효기간이 만료된 상품권 입니다.", StatusEnum.CONFLICT);
		}

		VoucherUsageHistory voucherUsageHistory = VoucherUsageHistory.builder()
				.member(memberService.read(username))
				.voucher(voucher.get())
				.amount(voucherUseRequestDto.getAmount())
				.place(voucherUseRequestDto.getPlace())
				.createdAt(LocalDateTime.now())
				.build();
		voucherUsageHistoryRepository.save(voucherUsageHistory);

		voucher.get().setBalance(remainingBalance - requestedAmount);
		voucherRepository.save(voucher.get());

		return VoucherUseResponseDto.builder()
				.usageId(voucherUsageHistory.getId())
				.voucherId(voucherId)
				.balance(remainingBalance - requestedAmount)
				.price(voucher.get().getProduct().getPrice())
				.build();
	}

	/*
	기프티콘 삭제 메서드
	 */
	public boolean delete(String username, Long voucherId) {
		Optional<Voucher> voucher = voucherRepository.findById(voucherId);
		List<Voucher> vouchers = voucherRepository.findAllByMemberUsername(username);

		if (voucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}

		if (!vouchers.contains(voucher.get())) {
			throw new BusinessException("상품권을 삭제할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		try {
			voucher.get().setDeletedAt(LocalDateTime.now());
			voucherRepository.save(voucher.get());
			return true;
		} catch (Exception e) {
			throw new BusinessException("상품권 삭제에 실패했습니다.", StatusEnum.BAD_REQUEST);
		}
	}

	/*
	Entity를 Dto로 변환하는 메서드
	 */
	public VoucherReadResponseDto mapToDto(Voucher voucher) {
		VoucherReadResponseDto voucherReadResponseDto = VoucherReadResponseDto.builder()
				.id(voucher.getId())
				.productId(voucher.getProduct().getId())
				.barcode(voucher.getBarcode())
				.price(voucher.getProduct().getPrice())
				.balance(voucher.getBalance())
				.expiresAt(voucher.getExpiresAt().toString())
				.imageUrl(voucher.getImageUrl())
				.build();
		return voucherReadResponseDto;
	}

	/**
	 *  사용자 별 중복 기프티콘 검사 메서드
	 */
	public boolean isDuplicateVoucher(String username, String barcode) {
		List<Voucher> vouchers = voucherRepository.findAllByMemberUsername(username);
		for (Voucher voucher : vouchers) {
			if (voucher.getBarcode().equals(barcode)) {
				return true;
			}
		}
		return false;
	}
}
