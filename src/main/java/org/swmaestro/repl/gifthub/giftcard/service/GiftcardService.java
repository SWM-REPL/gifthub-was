package org.swmaestro.repl.gifthub.giftcard.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
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

	public boolean isExist(Long id) {
		return giftCardRepository.existsByVoucherId(id);
	}

	public String generateUUID() {
		return UUID.randomUUID().toString();
	}

	public String generatePassword() {
		int random = new Random().nextInt(10000);
		return String.format("%04d", random);
	}
}
