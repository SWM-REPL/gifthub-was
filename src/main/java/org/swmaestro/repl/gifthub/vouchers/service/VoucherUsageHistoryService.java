package org.swmaestro.repl.gifthub.vouchers.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;
import org.swmaestro.repl.gifthub.vouchers.entity.VoucherUsageHistory;
import org.swmaestro.repl.gifthub.vouchers.repository.VoucherUsageHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherUsageHistoryService {
	private final VoucherUsageHistoryRepository voucherUsageHistoryRepository;
	private final MemberService memberService;

	/**
	 * 사용 내역 저장 메서드
	 */
	public Long create(Voucher voucher, VoucherUseRequestDto voucherUseRequestDto, String username) {
		VoucherUsageHistory voucherUsageHistory = VoucherUsageHistory.builder()
				.member(memberService.read(username))
				.voucher(voucher)
				.amount(voucherUseRequestDto.getAmount())
				.place(voucherUseRequestDto.getPlace())
				.build();
		voucherUsageHistoryRepository.save(voucherUsageHistory);
		return voucherUsageHistory.getId();
	}
}
