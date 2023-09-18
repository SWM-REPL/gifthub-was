package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final NaverService naverService;
	private final DeviceTokenService deviceTokenService;

	public TokenDto signIn(SignInDto loginDto) {
		Member member = memberRepository.findByUsername(loginDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
		}
		if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.BAD_REQUEST);
		}
		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	@Transactional
	public void signOut(String username, SignOutDto signOutDto) {
		Member member = memberRepository.findByUsername(username);
		if (member == null) {
			throw new BusinessException("존재하지 않는 사용자입니다.", StatusEnum.UNAUTHORIZED);
		}
		refreshTokenService.deleteRefreshToken(username);
		deviceTokenService.delete(signOutDto.getDeviceToken());
	}
}
