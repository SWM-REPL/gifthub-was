package org.swmaestro.repl.gifthub.giftcard.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.giftcard.config.GiftcardConfig;
import org.swmaestro.repl.gifthub.giftcard.dto.GiftcardResponseDto;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;
import org.swmaestro.repl.gifthub.giftcard.repository.GiftcardRepository;
import org.swmaestro.repl.gifthub.util.ByteArrayUtils;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherRepository;
import org.swmaestro.repl.gifthub.vouchers.service.BrandService;
import org.swmaestro.repl.gifthub.vouchers.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GiftcardService {
	private final GiftcardRepository giftCardRepository;
	private final GiftcardConfig giftcardConfig;
	private final AesBytesEncryptor aesBytesEncryptor;
	private final UserService userService;
	private final VoucherRepository voucherRepository;  // 순환 참조로 인해 vocuherService를 사용할 수 없음
	private final BrandService brandService;
	private final ProductService productService;

	/**
	 * 기프트 카드를 생성합니다.
	 * @param voucher: 기프트 카드를 생성할 기프티콘
	 * @param message: 기프트 카드에 담을 메시지
	 * @return: 생성된 기프트 카드의 id
	 */
	public VoucherShareResponseDto create(Voucher voucher, String message) {
		if (isExist(voucher.getId())) {
			throw new BusinessException("이미 공유된 기프티콘입니다.", StatusEnum.BAD_REQUEST);
		}

		Giftcard giftCard = Giftcard.builder()
				.id(generateUUID())
				.voucher(voucher)
				.password(encryptPassword(generatePassword()))
				.message(message)
				.expiresAt(LocalDateTime.now().plusDays(giftcardConfig.getEffectiveDay()))
				.build();
		giftCardRepository.save(giftCard);

		return VoucherShareResponseDto.builder()
				.id(giftCard.getId())
				.password(decryptPassword(giftCard.getPassword()))
				.build();
	}

	/**
	 * 기프트 카드를 조회합니다.
	 * @param id: 조회할 기프트 카드의 id
	 * @return: 조회된 기프트 카드
	 */
	public Giftcard read(String id) {
		if (!isExist(id)) {
			throw new BusinessException("존재하지 않는 기프트 카드입니다.", StatusEnum.NOT_FOUND);
		}

		return giftCardRepository.findById(id).get();
	}

	public Giftcard read(Long voucherId) {
		return giftCardRepository.findAllByVoucherId(voucherId)
				.filter(giftcard -> giftcard.getExpiresAt().isAfter(LocalDateTime.now()))
				.orElseThrow(() -> new BusinessException("존재하지 않는 기프트 카드입니다.", StatusEnum.NOT_FOUND));
	}

	/**
	 * 기프트 카드를 조회합니다.
	 * @param id: 조회할 기프트 카드의 id
	 * @param password: 조회할 기프트 카드의 비밀번호
	 * @return: 조회된 기프트 카드
	 */
	public GiftcardResponseDto read(String id, String password) {
		Giftcard giftcard = read(id);

		if (!giftcard.isEnable()) {
			throw new BusinessException("기간이 만료된 링크입니다.", StatusEnum.BAD_REQUEST);
		}

		if (giftcard.getInvalidPasswordCount() >= 10) {
			throw new BusinessException("비밀번호를 10회 이상 틀렸습니다. 이 링크는 더 이상 사용할 수 없습니다.", StatusEnum.FORBIDDEN);
		}

		if (!decryptPassword(giftcard.getPassword()).equals(password)) {
			save(giftcard.increaseInvalidPasswordCount());
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.FORBIDDEN);
		}

		save(giftcard.resetInvalidPasswordCount());

		return GiftcardResponseDto.builder()
				.sender(giftcard.getVoucher().getUser().getNickname())
				.message(giftcard.getMessage())
				.brand(brandService.readById(giftcard.getVoucher().getBrand().getId()))
				.product(productService.readById(giftcard.getVoucher().getProduct().getId()))
				.expiresAt(giftcard.getExpiresAt().toLocalDate())
				.build();
	}

	public Giftcard save(Giftcard giftcard) {
		return giftCardRepository.save(giftcard);
	}

	/**
	 * 기프트 카드의 소유자를 변경합니다.
	 * @param giftcardId: 변경할 기프트 카드의 id
	 * @param username: 변경할 소유자의 username
	 * @return: 변경된 기프트 카드
	 */
	public Giftcard changeVoucherUser(String giftcardId, String username) {
		Giftcard giftcard = read(giftcardId);

		if (giftcard.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException("만료된 링크입니다.", StatusEnum.BAD_REQUEST);
		}

		Voucher updatedVoucher = giftcard.getVoucher();
		User newUser = userService.read(username);
		updatedVoucher.setUser(newUser);
		voucherRepository.save(updatedVoucher);
		giftCardRepository.save(giftcard.expire());
		return giftcard;
	}

	/**
	 * 기프트 카드가 존재하는지 확인합니다. (기프트 카드 id로 확인)
	 * @param id
	 * @return
	 */
	public boolean isExist(String id) {
		return giftCardRepository.existsById(id);
	}

	/**
	 * 기프트 카드가 존재하는지 확인합니다. (기프트 카드의 voucher id로 확인)
	 * @param voucherId: 기프트 카드의 id
	 * @return: 기프트 카드가 존재하는지 여부
	 */
	public boolean isExist(Long voucherId) {
		if (giftCardRepository.existsByVoucherId(voucherId)) {
			if (giftCardRepository.findAllByVoucherId(voucherId).get().getExpiresAt().isAfter(LocalDateTime.now())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * UUID를 생성하는 메서드
	 * @return: 생성된 UUID
	 */
	public String generateUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 4자리의 숫자로 구성된 비밀번호를 생성
	 * @return: 생성된 비밀번호
	 */
	public String generatePassword() {
		int random = new Random().nextInt(10000);
		return String.format("%04d", random);
	}

	/**
	 * 비밀번호 복호화 메서드
	 * @param password: 복호화할 비밀번호
	 * @return: 복호화된 비밀번호
	 */
	private String decryptPassword(String password) {
		byte[] bytes = ByteArrayUtils.stringToByteArray(password);
		byte[] decrypt = aesBytesEncryptor.decrypt(bytes);
		return new String(decrypt, StandardCharsets.UTF_8);
	}

	/**
	 * 비밀번호 암호화 메서드
	 * @param password: 암호화할 비밀번호
	 * @return: 암호화된 비밀번호
	 */
	private String encryptPassword(String password) {
		byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
		byte[] encrypt = aesBytesEncryptor.encrypt(bytes);
		return ByteArrayUtils.byteArrayToString(encrypt);
	}

	public Giftcard delete(String id) {
		Giftcard giftcard = giftCardRepository.findById(id).orElseThrow(() -> new BusinessException("존재하지 않는 기프트 카드입니다.", StatusEnum.NOT_FOUND));
		giftcard.expire();
		save(giftcard);
		return giftcard;
	}
}
