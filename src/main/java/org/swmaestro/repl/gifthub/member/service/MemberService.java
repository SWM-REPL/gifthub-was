package org.swmaestro.repl.gifthub.member.service;

import org.swmaestro.repl.gifthub.member.dto.SignUpDTO;
import org.swmaestro.repl.gifthub.member.entity.Member;

import java.util.List;

public interface MemberService {
	Long create(SignUpDTO signUpDTO);

	Member read(Long id);

	int count();

	List<Member> list();

	Long update(Long id, Member member);

	Long delete(Long id);
}
