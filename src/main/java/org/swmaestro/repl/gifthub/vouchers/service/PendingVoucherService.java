package org.swmaestro.repl.gifthub.vouchers.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.entity.PendingVoucher;
import org.swmaestro.repl.gifthub.vouchers.repository.PendingVoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingVoucherService {
	private final PendingVoucherRepository pendingVoucherRepository;

	public Long create(User member) {
		PendingVoucher pendingVoucher = PendingVoucher.builder()
				.user(member)
				.build();
		pendingVoucherRepository.save(pendingVoucher);
		return pendingVoucher.getId();
	}

	public void delete(Long id) {
		Optional<PendingVoucher> pendingVoucher = pendingVoucherRepository.findById(id);
		//예외 처리
		if (pendingVoucher.isEmpty()) {
			throw new BusinessException("PendingVoucher가 존재하지 않습니다.", StatusEnum.NOT_FOUND);
		}
		pendingVoucherRepository.delete(pendingVoucher.get());
	}

	public int count(Long memberId) {
		return pendingVoucherRepository.countByUserId(memberId);
	}
}