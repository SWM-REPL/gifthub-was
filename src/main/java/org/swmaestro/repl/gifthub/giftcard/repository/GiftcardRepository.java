package org.swmaestro.repl.gifthub.giftcard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;

public interface GiftcardRepository extends JpaRepository<Giftcard, String> {
	public boolean existsByVoucherId(Long id);
}
