package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;
import org.swmaestro.repl.gifthub.giftcard.service.GiftcardService;
import org.swmaestro.repl.gifthub.util.DateConverter;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherListResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;
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
	private final UserService userService;
	private final PendingVoucherService pendingVoucherService;
	private final VoucherUsageHistoryService voucherUsageHistoryService;
	private final GiftcardService giftCardService;

	/*
		기프티콘 수동 저장 메서드
	 */
	public VoucherSaveResponseDto save(String username, VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		Brand brand = brandService.read(voucherSaveRequestDto.getBrandName()).orElseGet(() -> {
			Optional<Brand> defaultBrand = brandService.read("기타");
			return defaultBrand.get();
		});
		Product product = productService.read(brand.getId(), voucherSaveRequestDto.getProductName()).orElseGet(() -> {
			Product newProduct = Product.builder()
					.brand(brand)
					.name(voucherSaveRequestDto.getProductName())
					.imageUrl(storageService.getDefaultImagePath(voucherDirName))
					.isReusable(1)
					.build();
			return productService.save(newProduct);
		});
		String imageUrl = voucherSaveRequestDto.getImageUrl();

		if (isDuplicateVoucher(username, voucherSaveRequestDto.getBarcode()) == true) {
			throw new BusinessException("이미 등록된 기프티콘입니다.", StatusEnum.CONFLICT);
		}

		if (imageUrl != null) {
			imageUrl = storageService.getBucketAddress(voucherDirName) + voucherSaveRequestDto.getImageUrl();
		}

		if (voucherSaveRequestDto.getBarcode() == null) {
			throw new BusinessException("바코드를 입력해주세요.", StatusEnum.BAD_REQUEST);
		}

		Voucher voucher = Voucher.builder()
				.brand(brand)
				.product(product)
				.barcode(voucherSaveRequestDto.getBarcode())
				.expiresAt(DateConverter.stringToLocalDate(voucherSaveRequestDto.getExpiresAt()))
				.imageUrl(imageUrl)
				.balance(product.getPrice())
				.user(userService.read(username))
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
		List<Voucher> vouchers = voucherRepository.findAllByUserUsername(username);

		if (voucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}
		if (!vouchers.contains(voucher.get())) {
			throw new BusinessException("상품권을 조회할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		// if (voucher.get().isDeleted()) {
		// 	throw new BusinessException("삭제된 상품권 입니다.", StatusEnum.BAD_REQUEST);
		// }

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
	public VoucherListResponseDto list(Long userId, String username) {
		if (!userService.read(userId).getUsername().equals(username)) {
			throw new BusinessException("상품권을 조회할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		List<Voucher> vouchers = voucherRepository.findAllByUserId(userId);
		List<Long> voucherIdList = new ArrayList<>();
		for (Voucher voucher : vouchers) {
			// 삭제된 기프티콘은 조회되지 않도록 함
			if (voucher.getDeletedAt() != null) {
				continue;
			}
			voucherIdList.add(voucher.getId());
		}

		return VoucherListResponseDto.builder()
				.voucherIds(voucherIdList)
				.pendingCount(pendingVoucherService.count(userId))
				.build();
	}

	/*
	사용자 별 기프티콘 목록 조회 메서드(username으로 조회)
	 */
	public List<Long> list(String username) {
		List<Voucher> vouchers = voucherRepository.findAllByUserUsername(username);
		List<Long> voucherIdList = new ArrayList<>();
		for (Voucher voucher : vouchers) {
			voucherIdList.add(voucher.getId());
		}
		return voucherIdList;
	}

	/*
	기프티콘 정보 수정 메서드
	 */
	public VoucherUpdateResponseDto update(Long voucherId, VoucherUpdateRequestDto voucherUpdateRequestDto) {
		Voucher voucher = voucherRepository.findById(voucherId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND));
		// Balance 수정
		if (voucherUpdateRequestDto.getBalance() != null) {
			Optional<Product> product = productService.read(voucher.getBrand().getId(), voucher.getProduct().getName());
			if (product.isEmpty()) {
				product = productService.read(brandService.read("기타").get().getId(), voucher.getProduct().getName());
			}

			Integer productPrice = product.get().getPrice();
			Integer voucherUpdateRequestBalance = voucherUpdateRequestDto.getBalance();

			if (productPrice != null && voucherUpdateRequestBalance > productPrice) {
				throw new BusinessException("잔액은 상품 가격보다 클 수 없습니다.", StatusEnum.BAD_REQUEST);
			}
		}
		voucher.setBarcode(
				voucherUpdateRequestDto.getBarcode() == null ? voucher.getBarcode() :
						voucherUpdateRequestDto.getBarcode());
		// Brand 수정
		Brand brand = null;
		if (voucherUpdateRequestDto.getBrandName() == null) {
			brand = voucher.getBrand();
			voucher.setBrand(brand);
		} else {
			brand = updateBrand(voucherUpdateRequestDto);
			voucher.setBrand(brand);
		}
		// Product 수정
		if (voucherUpdateRequestDto.getProductName() == null) {
			voucher.setProduct(voucher.getProduct());
		} else {
			voucher.setProduct(updateProduct(voucherUpdateRequestDto, brand, voucher.getProduct()));
		}
		voucher.setExpiresAt(voucherUpdateRequestDto.getExpiresAt() == null ? voucher.getExpiresAt() :
				DateConverter.stringToLocalDate(voucherUpdateRequestDto.getExpiresAt()));

		voucher.setBalance(voucherUpdateRequestDto.getBalance() == null ? voucher.getBalance() :
				voucherUpdateRequestDto.getBalance());

		// isChecked 수정
		voucher.setChecked(voucherUpdateRequestDto.getIsChecked() == null ? voucher.isChecked() :
				voucherUpdateRequestDto.getIsChecked());

		voucherRepository.save(voucher);

		return VoucherUpdateResponseDto.builder()
				.id(voucherId)
				.accessible(voucher.getDeletedAt() == null)
				.id(voucher.getId())
				.productId(voucher.getProduct().getId())
				.barcode(voucher.getBarcode())
				.price(voucher.getProduct().getPrice())
				.balance(voucher.getBalance())
				.expiresAt(voucher.getExpiresAt().toString())
				.imageUrl(voucher.getImageUrl())
				.accessible(voucher.getDeletedAt() == null)
				.shared(giftCardService.isExist(voucher.getId()))
				.checked(voucher.isChecked())
				.build();
	}

	/*
	기프티콘 사용 등록 메서드
	 */
	public VoucherUseResponseDto use(String username, Long voucherId, VoucherUseRequestDto voucherUseRequestDto) {
		validateVoucherUseRequest(voucherUseRequestDto);
		Voucher voucher = getValidVoucherForUser(username, voucherId);
		if (voucher.getProduct().getPrice() == null) {
			validateVoucherWithoutPriceInfo(voucher, voucherUseRequestDto);
		} else {
			validateVoucherWithPriceInfo(voucher, voucherUseRequestDto);
		}
		Long usageId = voucherUsageHistoryService.create(voucher, voucherUseRequestDto, username);
		if (voucher.getBalance() == null || voucher.getProduct().getIsReusable() == 0) {
			return use(voucher, voucherId, usageId);
		} else {
			return use(voucher, voucherUseRequestDto, voucherId, usageId);
		}
	}

	/*
	기프티콘 삭제 메서드
	 */
	public boolean delete(String username, Long voucherId) {
		Optional<Voucher> voucher = voucherRepository.findById(voucherId);
		List<Voucher> vouchers = voucherRepository.findAllByUserUsername(username);

		if (voucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}

		if (!vouchers.contains(voucher.get())) {
			throw new BusinessException("상품권을 삭제할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		try {
			voucher.get().setDeletedAt(LocalDateTime.now());
			voucher.get().setBarcode(null);
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
				.accessible(voucher.getDeletedAt() == null)
				.shared(giftCardService.isExist(voucher.getId()))
				.checked(voucher.isChecked())
				.build();
		return voucherReadResponseDto;
	}

	/**
	 *  사용자 별 중복 기프티콘 검사 메서드
	 */
	public boolean isDuplicateVoucher(String username, String barcode) {
		List<Voucher> vouchers = voucherRepository.findAllByUserUsername(username);
		for (Voucher voucher : vouchers) {
			String voucherBarcode = voucher.getBarcode();
			if (voucherBarcode != null && voucherBarcode.equals(barcode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 기프티콘 사용 등록 메서드 (non-reusabel voucher 이거나 voucher balance가 null인 경우)
	 */
	public VoucherUseResponseDto use(Voucher voucher, Long voucherId, Long usageId) {
		voucher.setBalance(0);
		voucherRepository.save(voucher);
		return VoucherUseResponseDto.builder()
				.usageId(usageId)
				.voucherId(voucherId)
				.balance(0)
				.price(voucher.getProduct().getPrice())
				.build();
	}

	/**
	 * 기프티콘 사용 등록 메서드 (usable voucher)
	 */
	public VoucherUseResponseDto use(Voucher voucher, VoucherUseRequestDto voucherUseRequestDto,
			Long voucherId, Long usageId) {
		int remainingBalance = voucher.getBalance();
		int requestedAmount = voucherUseRequestDto.getAmount();

		if (requestedAmount > remainingBalance) {
			throw new BusinessException("잔액이 부족합니다.", StatusEnum.CONFLICT);
		}
		voucher.setBalance(remainingBalance - requestedAmount);
		voucherRepository.save(voucher);

		return VoucherUseResponseDto.builder()
				.usageId(usageId)
				.voucherId(voucherId)
				.balance(remainingBalance - requestedAmount)
				.price(voucher.getProduct().getPrice())
				.build();
	}

	/**
	 * 기프티콘 사용 요청 유효성 검사 메소드
	 */
	private void validateVoucherUseRequest(VoucherUseRequestDto voucherUseRequestDto) {
		if (voucherUseRequestDto.getAmount() != null && voucherUseRequestDto.getAmount() <= 0) {
			throw new BusinessException("사용 금액을 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
		if (voucherUseRequestDto.getPlace() == null) {
			throw new BusinessException("사용처를 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
	}

	/**
	 * Voucher 유효성 검사 메서드
	 */
	private Voucher getValidVoucherForUser(String username, Long voucherId) {
		Optional<Voucher> optionalVoucher = voucherRepository.findById(voucherId);
		if (optionalVoucher.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND);
		}

		Voucher voucher = optionalVoucher.get();
		if (!voucherRepository.findAllByUserUsername(username).contains(voucher)) {
			throw new BusinessException("상품권을 사용할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		if (voucher.getBalance() != null && voucher.getBalance() == 0) {
			throw new BusinessException("이미 사용된 상품권 입니다.", StatusEnum.NOT_FOUND);
		}
		if (voucher.getExpiresAt().isBefore(LocalDate.now())) {
			throw new BusinessException("유효기간이 만료된 상품권 입니다.", StatusEnum.CONFLICT);
		}
		return voucher;
	}

	/**
	 * DB에 없는 정보의 기프티콘 사용 요청 유효성 검사 메서드
	 */
	private void validateVoucherWithoutPriceInfo(Voucher voucher, VoucherUseRequestDto voucherUseRequestDto) {
		if (voucher.getBalance() == null && voucherUseRequestDto.getAmount() != null) {
			throw new BusinessException("사용 금액을 입력할 수 없는 상품권입니다.", StatusEnum.BAD_REQUEST);
		}

		if (voucher.getBalance() != null && voucherUseRequestDto.getAmount() == null) {
			throw new BusinessException("사용 금액을 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
	}

	/**
	 * DB에 있는 정보의 기프티콘 사용 요청 유효성 검사 메서드
	 */
	private void validateVoucherWithPriceInfo(Voucher voucher, VoucherUseRequestDto voucherUseRequestDto) {
		if (voucher.getProduct().getIsReusable() == 1) {
			if (voucherUseRequestDto.getAmount() == null) {
				throw new BusinessException("사용 금액을 입력해주세요.", StatusEnum.BAD_REQUEST);
			}
		} else if (voucherUseRequestDto.getAmount() != null) {
			throw new BusinessException("사용 금액을 입력할 수 없는 상품권입니다.", StatusEnum.BAD_REQUEST);
		}
	}

	/**
	 * 기프티콘 정보 수정 메서드 (수정하려고 하는 product name이 존재하지 않을 경우)
	 */
	public Product updateProduct(VoucherUpdateRequestDto voucherUpdateRequestDto, Brand brand, Product oldProduct) {
		Optional<Product> optionalProduct = productService.read(brand.getId(), voucherUpdateRequestDto.getProductName());

		if (optionalProduct.isEmpty()) {
			Brand otherBrand = brandService.read("기타").orElseThrow(
					() -> new BusinessException("해당 상품이 존재하지 않습니다.", StatusEnum.NOT_FOUND));
			optionalProduct = productService.read(otherBrand.getId(), voucherUpdateRequestDto.getProductName());
		}

		return optionalProduct.orElseGet(() -> {
			Product newProduct = Product.builder()
					.brand(brandService.read("기타").orElseThrow(
							() -> new BusinessException("기타 브랜드를 찾을 수 없습니다.", StatusEnum.NOT_FOUND)))
					.name(voucherUpdateRequestDto.getProductName())
					.isReusable(1)
					.price(oldProduct.getPrice())
					.imageUrl(storageService.getDefaultImagePath(voucherDirName))
					.build();
			return productService.save(newProduct);
		});
	}

	/**
	 * 기프티콘 정보 수정 메서드 (Brand name)
	 */
	public Brand updateBrand(VoucherUpdateRequestDto voucherUpdateRequestDto) {
		Brand brand = brandService.read(voucherUpdateRequestDto.getBrandName()).orElseGet(() -> {
			Optional<Brand> defaultBrand = brandService.read("기타");
			return defaultBrand.get();
		});
		return brand;
	}

	/**
	 * 기프티콘 공유 요청 메서드
	 */
	public VoucherShareResponseDto share(String username, Long voucherId, VoucherShareRequestDto voucherShareRequestDto) {
		Voucher voucher = voucherRepository.findById(voucherId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND));
		if (!voucher.getUser().getUsername().equals(username)) {
			throw new BusinessException("상품권을 공유할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		if (voucher.getDeletedAt() != null) {
			throw new BusinessException("삭제된 상품권은 공유할 수 없습니다.", StatusEnum.BAD_REQUEST);
		}
		if (voucherShareRequestDto.getMessage() == null) {
			throw new BusinessException("전달할 메시지를 입력해주세요.", StatusEnum.BAD_REQUEST);
		}

		return giftCardService.create(voucher, voucherShareRequestDto.getMessage());
	}

	/**
	 * 기프티콘 공유 취소 메서드
	 */
	public Giftcard cancelShare(String username, Long voucherId) {
		Voucher voucher = voucherRepository.findById(voucherId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 상품권 입니다.", StatusEnum.NOT_FOUND));

		if (!voucher.getUser().getUsername().equals(username)) {
			throw new BusinessException("상품권을 공유 취소할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}

		Giftcard savedGiftcard = giftCardService.read(voucherId);
		if (savedGiftcard == null) {
			throw new BusinessException("존재하지 않는 공유 기프티콘 입니다.", StatusEnum.NOT_FOUND);
		}

		return giftCardService.delete(savedGiftcard.getId());
	}

	/**
	 * 기프티콘 이미지 조회를 위한 presigned url 생성 메서드를 위한 filename 추출 메서드
	 */
	public String getPresignedUrlForVoucherImage(String username, Long voucherId) {
		String imageUrl = read(voucherId, username).getImageUrl();
		String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		return storageService.getPresignedUrl(voucherDirName, filename);
	}

	/**
	 * 사용자 별 기프티콘 전체 삭제 메서드
	 * @param userId
	 */
	public void deleteAll(Long userId) {
		voucherRepository.findAllByUserId(userId).stream().forEach(voucher -> {
			voucher.setDeletedAt(LocalDateTime.now());
			voucherRepository.save(voucher);
		});
	}
}