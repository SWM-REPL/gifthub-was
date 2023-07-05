package org.swmaestro.repl.gifthub.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.member.entity.Member;

public interface SpringDataJpaMemberRepository extends JpaRepository<Member, Long> {
	Member findByUsername(String username);
}
