package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.Member;

public interface VoucherRepository extends JpaRepository<Member, Long> {

}
