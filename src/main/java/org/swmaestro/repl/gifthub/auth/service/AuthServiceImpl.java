package org.swmaestro.repl.gifthub.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.ErrorCode;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	public TokenDto signIn(SignInDto loginDto) {
		Member member = memberRepository.findByUsername(loginDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", ErrorCode.INVALID_INPUT_VALUE);
		}
		if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", ErrorCode.INVALID_INPUT_VALUE);
		}
		String accessToken = jwtProvider.generateToken(member.getUsername());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	@Transactional
	public void signOut(String username) {
		Member member = memberRepository.findByUsername(username);
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", ErrorCode.INVALID_INPUT_VALUE);
		}
		refreshTokenService.deleteRefreshToken(username);
	}
}
