package org.swmaestro.repl.gifthub.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.entity.Member;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
	Optional<DeviceToken> findByToken(String token);

	Optional<DeviceToken> findByMemberAndToken(Member member, String token);

	List<DeviceToken> findAllByMember(Member member);
}
