package org.swmaestro.repl.gifthub.giftcard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;

public interface GiftcardRepository extends JpaRepository<Giftcard, String> {
	boolean existsByVoucherId(Long id);

	Optional<Giftcard> findAllByVoucherId(Long id);
}
