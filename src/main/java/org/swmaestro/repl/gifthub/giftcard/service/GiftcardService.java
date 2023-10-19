package org.swmaestro.repl.gifthub.giftcard.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.giftcard.dto.GiftcardResponseDto;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;
import org.swmaestro.repl.gifthub.giftcard.repository.GiftcardRepository;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GiftcardService {
	private final GiftcardRepository giftCardRepository;

	public VoucherShareResponseDto create(Voucher voucher, String message) {
		if (isExist(voucher.getId())) {
			throw new BusinessException("이미 공유된 기프티콘입니다.", StatusEnum.BAD_REQUEST);
		}
		Giftcard giftCard = Giftcard.builder()
				.id(generateUUID())
				.voucher(voucher)
				.password(generatePassword())
				.message(message)
				.expiresAt(LocalDateTime.now().plusDays(3))
				.build();
		giftCardRepository.save(giftCard);

		return VoucherShareResponseDto.builder()
				.id(giftCard.getId())
				.build();
	}

	public Giftcard read(String id) {
		if (!isExist(id)) {
			throw new BusinessException("존재하지 않는 링크입니다.", StatusEnum.NOT_FOUND);
		}

		return giftCardRepository.findById(id).get();
	}

	public GiftcardResponseDto read(String id, String password) {
		Giftcard giftcard = read(id);

		if (giftcard.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new BusinessException("만료된 링크입니다.", StatusEnum.BAD_REQUEST);
		}

		if (!giftcard.getPassword().equals(password)) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.FORBIDDEN);
		}

		return GiftcardResponseDto.builder()
				.sender(giftcard.getVoucher().getUser().getNickname())
				.message(giftcard.getMessage())
				.brandName(giftcard.getVoucher().getBrand().getName())
				.productName(giftcard.getVoucher().getProduct().getName())
				.expiresAt(giftcard.getExpiresAt().toLocalDate())
				.build();
	}

	public boolean isExist(String id) {
		return giftCardRepository.existsById(id);
	}

	public boolean isExist(Long voucherId) {
		return giftCardRepository.existsByVoucherId(voucherId);
	}

	public String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public String generatePassword() {
		int random = new Random().nextInt(10000);
		return String.format("%04d", random);
	}
}
