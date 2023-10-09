package org.swmaestro.repl.gifthub.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
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
	public TokenDto signIn(SignInDto signInDto) {
		Member member = memberService.read(signInDto.getUsername());

		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
		}

		if (!passwordEncoder.matches(signInDto.getPassword(), member.getPassword())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.BAD_REQUEST);
		}

		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		return tokenDto;
	}

	// /**
	//  * 소셜 로그인(회원가입)
	//  * @param oAuth2UserInfoDto
	//  * @param platform
	//  * @return
	//  */
	// public TokenDto signIn(OAuth2UserInfoDto oAuth2UserInfoDto, OAuthPlatform platform) {
	// 	boolean isExists = false;
	//
	// 	switch (platform) {
	// 		case NAVER:
	// 			isExists = naverService.isExists(oAuth2UserInfoDto);
	// 			break;
	// 		default:
	// 			throw new IllegalArgumentException("지원하지 않는 플랫폼입니다.");
	// 	}
	//
	// 	if (!isExists) {
	// 		naverService.save()
	// 	}
	// }

	private SignInDto convertMemberToSignInDto(Member member) {
		return SignInDto.builder()
				.username(member.getUsername())
				.password(member.getPassword())
				.build();
	}

	// public TokenDto signIn(SignInDto loginDto) {
	// 	Member member = memberRepository.findByUsername(loginDto.getUsername());
	// 	if (member == null) {
	// 		throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
	// 	}
	// 	if (!passwordEncoder.matches(loginDto.getPassword(), member.getPassword())) {
	// 		throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.BAD_REQUEST);
	// 	}
	// 	String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
	// 	String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());
	//
	// 	TokenDto tokenDto = TokenDto.builder()
	// 			.accessToken(accessToken)
	// 			.refreshToken(refreshToken)
	// 			.build();
	//
	// 	refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());
	//
	// 	return tokenDto;
	// }
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
