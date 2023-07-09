package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Member findByUsername(String username);

	Member findByNickname(String nickname);
}
