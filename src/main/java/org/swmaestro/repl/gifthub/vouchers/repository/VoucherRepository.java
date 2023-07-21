package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import java.util.List;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
	List<Voucher> findByMember_Username(String username);

	List<Voucher> findByMember_UsernameAndBrand_Name(String username, String brandName);

}
