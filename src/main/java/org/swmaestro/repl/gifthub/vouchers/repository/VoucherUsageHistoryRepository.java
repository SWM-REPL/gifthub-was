package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.VoucherUsageHistory;

import java.util.List;

public interface VoucherUsageHistoryRepository extends JpaRepository<VoucherUsageHistory, Long> {
	List<VoucherUsageHistory> findAllByVoucherId(Long voucherId);
}
