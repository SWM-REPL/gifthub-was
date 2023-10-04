package org.swmaestro.repl.gifthub.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {
	Optional<OAuth> findByMemberAndPlatform(Member member, OAuthPlatform platform);
}
