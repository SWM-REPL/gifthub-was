package org.swmaestro.repl.gifthub.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.LoginDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public LoginDto verifyPassword(LoginDto loginDto) {
		Member member = memberRepository.findByUsername(loginDto.getUsername());

		if (member != null && passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
			return LoginDto.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.build();
		}
		return null;
	}
}
