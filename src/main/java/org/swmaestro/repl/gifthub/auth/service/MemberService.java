package org.swmaestro.repl.gifthub.auth.service;

import java.util.List;

import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;

public interface MemberService {
	TokenDto create(SignUpDto signUpDTO);

	Member convertSignUpDTOtoMember(SignUpDto signUpDTO);

	boolean isDuplicateUsername(String username);

	Member passwordEncryption(Member member);

	Member read(String username);

	int count();

	List<Member> list();

	MemberUpdateResponseDto update(String username, Long userId, MemberUpdateRequestDto memberUpdateRequestDto);

	MemberDeleteResponseDto delete(Long id);
}
