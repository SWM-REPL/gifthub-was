package org.swmaestro.repl.gifthub.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;

public interface OAuthRepository extends JpaRepository<OAuth, Long> {
	Optional<OAuth> findByUserAndPlatform(User user, OAuthPlatform platform);

	Optional<OAuth> findByPlatformAndPlatformId(OAuthPlatform platform, String platformId);

	Optional<OAuth> deleteByUserAndPlatform(User user, OAuthPlatform platform);

	List<OAuth> findAllByUser(User user);

	Optional<OAuth> findByPlatformAndPlatformIdAndDeletedAtIsNull(OAuthPlatform platform, String platformId);

	List<OAuth> findAllByUserAndDeletedAtIsNull(User user);
}
