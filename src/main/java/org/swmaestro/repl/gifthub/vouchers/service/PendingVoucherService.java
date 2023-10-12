package org.swmaestro.repl.gifthub.vouchers.service;

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

	public void create(User user) {
		PendingVoucher pendingVoucher = PendingVoucher.builder()
				.user(user)
				.build();
		pendingVoucherRepository.save(pendingVoucher);
	}

	public void delete(User user) {
		PendingVoucher pendingVoucher = pendingVoucherRepository.findByMemberId(user.getId());
		//예외 처리
		if (pendingVoucher == null) {
			throw new BusinessException("PendingVoucher가 존재하지 않습니다.", StatusEnum.NOT_FOUND);
		}
		pendingVoucherRepository.delete(pendingVoucher);
	}

	public int count(Long memberId) {
		return pendingVoucherRepository.countByMemberId(memberId);
	}
}
