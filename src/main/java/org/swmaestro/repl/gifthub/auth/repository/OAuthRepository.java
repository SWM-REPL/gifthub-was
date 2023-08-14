package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {
	// List<OAuth> findAllByMember(Long memberId);
	//
	// OAuth findByMemberAndPlatform(Long memberId, OAuthPlatform platform);
}
