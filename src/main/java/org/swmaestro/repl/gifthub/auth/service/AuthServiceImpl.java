package org.swmaestro.repl.gifthub.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	public SignInDto signIn(SignInDto loginDto) {
		Member member = memberRepository.findByUsername(loginDto.getUsername());

		if (member != null && passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
			return SignInDto.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.build();
		}
		return null;
	}
}
