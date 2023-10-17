package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.config.AuthConfig;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.UserRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.auth.type.Role;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final OAuthService oAuthService;
	private final UserRepository userRepository;
	private final RefreshTokenService refreshTokenService;
	private final DeviceTokenService deviceTokenService;
	private final AuthConfig authConfig;

	/**
	 * 회원가입
	 * @param signUpDto
	 */
	public JwtTokenDto signUp(SignUpDto signUpDto) {
		User user = User.builder()
				.username(signUpDto.getUsername())
				.password(passwordEncoder.encode(signUpDto.getPassword()))
				.nickname(signUpDto.getNickname())
				.role(Role.USER)
				.build();

		User savedUser = userService.create(user);
		return generateJwtTokenDto(savedUser);
	}

	/**
	 * 일반 로그인
	 * @param signInDto
	 */
	public JwtTokenDto signIn(SignInDto signInDto) {
		User user = userService.read(signInDto.getUsername());

		if (user == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
		}

		if (!passwordEncoder.matches(signInDto.getPassword(), user.getPassword())) {
			throw new BusinessException("비밀번호가 일치하지 않습니다.", StatusEnum.BAD_REQUEST);
		}

		String accessToken = jwtProvider.generateToken(user.getUsername(), user.getId());
		String refreshToken = jwtProvider.generateRefreshToken(user.getUsername(), user.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
		refreshTokenService.storeRefreshToken(jwtTokenDto, user.getUsername());
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
			User newUser = User.builder()
					.username(userService.generateOAuthUsername())
					.nickname(authConfig.getDefaultNickname())
					.password(authConfig.getDefaultPassword())
					.role(Role.USER)
					.build();
			// 회원 정보 저장
			User user = userService.create(newUser);
			// oauth 정보 저장
			oAuth = oAuthService.create(user, userInfo, platform);
		}
		JwtTokenDto jwtTokenDto = generateJwtTokenDto(oAuth.getUser());
		refreshTokenService.storeRefreshToken(jwtTokenDto, oAuth.getUser().getUsername());
		return jwtTokenDto;
	}

	/**
	 * JWT 토큰 생성
	 * @param user
	 * @return
	 */
	private JwtTokenDto generateJwtTokenDto(User user) {
		String accessToken = jwtProvider.generateToken(user.getUsername(), user.getId());
		String refreshToken = jwtProvider.generateRefreshToken(user.getUsername(), user.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		return jwtTokenDto;
	}

	/**
	 * 로그아웃
	 * @param username
	 * @param signOutDto
	 */
	@Transactional
	public void signOut(String username, SignOutDto signOutDto) {
		User user = userRepository.findByUsername(username);
		if (user == null || user.getDeletedAt() != null) {
			throw new BusinessException("존재하지 않는 사용자입니다.", StatusEnum.UNAUTHORIZED);
		}
		refreshTokenService.deleteRefreshToken(username);
	}
}
