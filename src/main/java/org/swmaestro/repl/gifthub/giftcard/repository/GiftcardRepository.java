package org.swmaestro.repl.gifthub.giftcard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;

public interface GiftcardRepository extends JpaRepository<Giftcard, String> {
	boolean existsByVoucherId(Long id);

	List<Giftcard> findAllByVoucherId(Long id);
}
