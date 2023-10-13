package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.config.AuthConfig;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final OAuthService oAuthService;
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final DeviceTokenService deviceTokenService;
	private final AuthConfig authConfig;

	/**
	 * 회원가입
	 * @param signUpDto
	 */
	public JwtTokenDto signUp(SignUpDto signUpDto) {
		Member member = Member.builder()
				.username(signUpDto.getUsername())
				.password(passwordEncoder.encode(signUpDto.getPassword()))
				.nickname(signUpDto.getNickname())
				.build();

		Member savedMember = memberService.create(member);
		return generateJwtTokenDto(savedMember);
	}

	/**
	 * 일반 로그인
	 * @param signInDto
	 */
	public JwtTokenDto signIn(SignInDto signInDto) {
		Member member = memberService.read(signInDto.getUsername());

		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
		}

		if (!passwordEncoder.matches(signInDto.getPassword(), member.getPassword())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.BAD_REQUEST);
		}

		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		return jwtTokenDto;
	}

	/**
	 * OAuth 로그인
	 * @param oAuthTokenDto
	 * @param platform
	 * @return
	 */
	public JwtTokenDto signIn(OAuthTokenDto oAuthTokenDto, OAuthPlatform platform) {
		OAuth oAuth;

		OAuthUserInfoDto userInfo = oAuthService.getUserInfo(oAuthTokenDto, platform);
		if (oAuthService.isExists(userInfo, platform)) {
			// 존재할 경우 -> 로그인
			oAuth = oAuthService.read(userInfo, platform);
		} else {
			// 존재하지 않을 경우 -> 회원 가입 -> 로그인
			Member newMember = Member.builder()
					.username(memberService.generateOAuthUsername())
					.nickname(authConfig.getDefaultNickname())
					.password(passwordEncoder.encode(authConfig.getDefaultPassword()))
					.build();
			// 회원 정보 저장
			Member member = memberService.create(newMember);
			// oauth 정보 저장
			oAuth = oAuthService.create(member, userInfo, platform);
		}

		return generateJwtTokenDto(oAuth.getMember());
	}

	/**
	 * JWT 토큰 생성
	 * @param member
	 * @return
	 */
	private JwtTokenDto generateJwtTokenDto(Member member) {
		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(jwtTokenDto, member.getUsername());

		return jwtTokenDto;
	}

	/**
	 * 로그아웃
	 * @param username
	 */
	@Transactional
	public void signOut(String username) {
		Member member = memberRepository.findByUsername(username);
		System.out.println("member = " + member.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 사용자입니다.", StatusEnum.UNAUTHORIZED);
		}
		refreshTokenService.deleteRefreshToken(username);
	}
}
