package org.swmaestro.repl.gifthub.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> save(Member member);

	Member findByUsername(String username);

	Member findByNickname(String nickname);
}
