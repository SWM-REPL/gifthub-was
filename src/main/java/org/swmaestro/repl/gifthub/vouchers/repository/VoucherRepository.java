package org.swmaestro.repl.gifthub.vouchers.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
	List<Voucher> findAllByMemberUsername(String username);

	List<Voucher> findAllByMemberId(Long memberId);

}