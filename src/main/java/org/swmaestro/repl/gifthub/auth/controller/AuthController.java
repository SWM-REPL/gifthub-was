package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "사용자 인증 관련 API")
public class AuthController {
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;

	@PostMapping("/sign-up")
	@Operation(summary = "회원가입 메서드", description = "사용자가 회원가입을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400(400-1)", description = "비밀번호 형식 부적합(영문, 숫자, 특수문자를 포함한 8자리 이상)"),
			@ApiResponse(responseCode = "400(400-2)", description = "닉네임 글자 수 부적합(12자리 이하)"),
			@ApiResponse(responseCode = "400(409)", description = "이미 존재하는 아이디")
	})
	public ResponseEntity<Message> signUp(HttpServletRequest request, @RequestBody SignUpDto signUpDto) {
		JwtTokenDto jwtTokenDto = authService.signUp(signUpDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-in")
	@Operation(summary = "로그인 메서드", description = "사용자가 로그인을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "400(400-1)", description = "존재하지 않는 아이디"),
			@ApiResponse(responseCode = "400(400-2)", description = "비밀번호 불일치"),
			@ApiResponse(responseCode = "400(409)", description = "이미 존재하는 아이디")
	})
	public ResponseEntity<Message> signIn(HttpServletRequest request, @RequestBody SignInDto signInDto) {
		JwtTokenDto jwtTokenDto = authService.signIn(signInDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh Token을 이용한 New RefreshToken, New Access Token 발급 메서드", description = "Refresh Token을 이용하여 새로운 Refresh Token, Access Token을 발급 받기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Access Token 재발급 성공"),
			@ApiResponse(responseCode = "400", description = "Access Token 재발급 실패"),
	})
	public ResponseEntity<Message> reissueAccessToken(HttpServletRequest request, @RequestHeader("Authorization") String refreshToken) {
		String newAccessToken = refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken);
		String newRefreshToken = refreshTokenService.createNewRefreshTokenByValidateRefreshToken(refreshToken);

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();

		refreshToken = refreshToken.substring(7);
		refreshTokenService.storeRefreshToken(jwtTokenDto, jwtProvider.getUsername(refreshToken));

		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-in/naver")
	@Operation(summary = "네이버 로그인 메서드", description = "네이버 로그인 콜백을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "네이버 로그인 성공"),
			@ApiResponse(responseCode = "400", description = "네이버 로그인 실패"),
	})
	public ResponseEntity<Message> naverSignIn(HttpServletRequest request, @RequestBody OAuthTokenDto oAuthTokenDto) {
		JwtTokenDto jwtTokenDto = authService.signIn(oAuthTokenDto, OAuthPlatform.NAVER);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-in/kakao")
	@Operation(summary = "카카오 로그인 메서드", description = "카카오 로그인을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "카카오 로그인 성공"),
			@ApiResponse(responseCode = "400(400-1)", description = "잘못된 프로토콜 요청"),
			@ApiResponse(responseCode = "400(400-2)", description = "잘못된 URL 요쳥"),
			@ApiResponse(responseCode = "400(500)", description = "HTTP 연결 수행 실패"),
	})
	public ResponseEntity<Message> kakaoSignIn(HttpServletRequest request, @RequestBody OAuthTokenDto oAuthTokenDto) {
		JwtTokenDto jwtTokenDto = authService.signIn(oAuthTokenDto, OAuthPlatform.KAKAO);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-in/google")
	@Operation(summary = "구글 로그인 메서드", description = "구글로부터 사용자 정보를 얻어와 회원가입 및 로그인을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "구글 로그인 성공"),
			@ApiResponse(responseCode = "400(400)", description = "잘못된 프로토콜 혹은 URL 요쳥"),
			@ApiResponse(responseCode = "400(500)", description = "HTTP 연결 수행 실패"),
	})
	public ResponseEntity<Message> googleSignIn(HttpServletRequest request, @RequestBody OAuthTokenDto oAuthTokenDto) {
		JwtTokenDto jwtTokenDto = authService.signIn(oAuthTokenDto, OAuthPlatform.GOOGLE);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-in/apple")
	@Operation(summary = "애플 로그인 메서드", description = "애플 로그인을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "애플 로그인 성공"),
			@ApiResponse(responseCode = "400", description = "애플 로그인 실패"),
	})
	public ResponseEntity<Message> appleSignIn(HttpServletRequest request, @RequestBody OAuthTokenDto oAuthTokenDto) {
		JwtTokenDto jwtTokenDto = authService.signIn(oAuthTokenDto, OAuthPlatform.APPLE);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}

	@PostMapping("/sign-out")
	@Operation(summary = "로그아웃 메서드", description = "사용자가 로그아웃을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "로그인 성공"),
			@ApiResponse(responseCode = "400(401)", description = "존재하지 않는 사용자")
	})
	public ResponseEntity<Message> signOut(HttpServletRequest request, @RequestBody SignOutDto signOutDto) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		authService.signOut(username, signOutDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(null)
						.build());
	}

	@PostMapping("/sign-up/anonymous")
	@Operation(summary = "비회원 회원가입 메서드", description = "비회원 회원가입을 하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
	})
	public ResponseEntity<Message> signUpAnonymous(HttpServletRequest request) {
		JwtTokenDto jwtTokenDto = authService.signUpAnonymous();
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(jwtTokenDto)
						.build());
	}
}
