package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
	List<Voucher> findByMemberUsername(String username);

	List<Voucher> findByMemberUsernameAndBrandName(String username, String brandName);

}
