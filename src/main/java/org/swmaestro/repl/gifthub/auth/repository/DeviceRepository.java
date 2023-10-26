package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	void deleteByUsername(String userEmail);

	Optional<RefreshToken> findByUsername(String username);

}
