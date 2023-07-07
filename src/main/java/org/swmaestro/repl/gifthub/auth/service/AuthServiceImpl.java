package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.SpringDataJpaMemberRepository;

public class AuthServiceImpl implements AuthService {
	private final SpringDataJpaMemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthServiceImpl(SpringDataJpaMemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public SignUpDto verifyPassword(SignUpDto signUpDto) {
		Member member = memberRepository.findByUsername(signUpDto.getUsername());

		if (member != null && passwordEncoder.matches(signUpDto.getPassword(), member.getPassword())) {
			return SignUpDto.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.build();
		}
		return null;
	}
}
