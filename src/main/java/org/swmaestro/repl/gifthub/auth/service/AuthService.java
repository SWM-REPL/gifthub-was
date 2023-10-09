package org.swmaestro.repl.gifthub.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final MemberService memberService;
	private final PasswordEncoder passwordEncoder;
	private final NaverService naverService;
	private final JwtProvider jwtProvider;
	private final OAuthService oAuthService;

	/**
	 * 회원가입
	 * @param signUpDto
	 */
	public Member signUp(SignUpDto signUpDto) {
		Member member = Member.builder()
				.username(signUpDto.getUsername())
				.password(passwordEncoder.encode(signUpDto.getPassword()))
				.build();

		Optional<Member> savedMember = memberService.create(member);

		if (!savedMember.isPresent()) {
			throw new BusinessException("회원가입에 실패하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}

		return savedMember.get();
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

	public JwtTokenDto signIn(OAuthTokenDto oAuthTokenDto, OAuthPlatform platform) {
		OAuth oAuth;
		Member member;

		OAuthUserInfoDto userInfo = oAuthService.getUserInfo(oAuthTokenDto, platform);
		if (oAuthService.isExists(userInfo, platform)) {
			// 존재할 경우 -> 로그인
			oAuth = oAuthService.read(userInfo, platform);
		} else {
			// 존재하지 않을 경우 -> 회원 가입 -> 로그인
			Member newMember = Member.builder()
					.username(memberService.generateOAuthUsername())
					.build();
			// 회원 정보 저장
			member = memberService.create(newMember).get();
			// oauth 정보 저장
			oAuth = oAuthService.create(member, userInfo, platform);
		}

		return generateJwtTokenDto(oAuth.getMember());
	}

	public SignInDto convertMemberToSignInDto(Member member) {
		return SignInDto.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.build();
	}

	private JwtTokenDto generateJwtTokenDto(Member member) {
		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		return jwtTokenDto;
	}
	//
	// @Transactional
	// public void signOut(String username, SignOutDto signOutDto) {
	// 	Member member = memberRepository.findByUsername(username);
	// 	if (member == null) {
	// 		throw new BusinessException("존재하지 않는 사용자입니다.", StatusEnum.UNAUTHORIZED);
	// 	}
	// 	refreshTokenService.deleteRefreshToken(username);
	// 	deviceTokenService.delete(signOutDto.getDeviceToken());
	// }
}
