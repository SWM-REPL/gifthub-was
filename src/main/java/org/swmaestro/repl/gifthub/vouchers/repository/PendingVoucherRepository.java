package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.PendingVoucher;

public interface PendingVoucherRepository extends JpaRepository<PendingVoucher, Long> {
	PendingVoucher findByUserId(Long userId);

	int countByUserId(Long userId);
}
