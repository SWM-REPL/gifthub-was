package org.swmaestro.repl.gifthub.auth.controller;

import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.swmaestro.repl.gifthub.auth.dto.*;
import org.swmaestro.repl.gifthub.auth.service.*;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import java.io.IOException;
import java.security.PrivateKey;
import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "사용자 인증 관련 API")
public class AuthController {
	private final MemberService memberService;
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	private final NaverService naverService;
	private final KakaoService kakaoService;
	private final GoogleService googleService;
  private final AppleService appleService;

	@PostMapping("/sign-up")
	@Operation(summary = "회원가입 메서드", description = "사용자가 회원가입을 하기 위한 메서드입니다.")
	public TokenDto signUp(@RequestBody SignUpDto signUpDto) {
		TokenDto tokenDto = memberService.create(signUpDto);
		return tokenDto;
	}

	@PostMapping("/sign-in")
	@Operation(summary = "로그인 메서드", description = "사용자가 로그인을 하기 위한 메서드입니다.")
	public TokenDto signIn(@RequestBody SignInDto loginDto) {
		TokenDto tokenDto = authService.signIn(loginDto);
		return tokenDto;
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh Token 재발급 메서드", description = "Refresh Token을 재발급 받기 위한 메서드입니다.")
	public TokenDto validateRefreshToken(@RequestHeader("Authorization") String refreshToken) {
		String newAccessToken = refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken);
		String newRefreshToken = refreshTokenService.createNewRefreshTokenByValidateRefreshToken(refreshToken);

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();

		refreshToken = refreshToken.substring(7);
		refreshTokenService.storeRefreshToken(tokenDto, jwtProvider.getUsername(refreshToken));
		return tokenDto;
	}

	@GetMapping("/sign-in/naver")
	@Operation(summary = "네이버 로그인 메서드", description = "네이버 로그인을 하기 위한 메서드입니다.")
	public void naverLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(naverService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/naver/callback")
	@Operation(summary = "네이버 로그인 콜백 메서드", description = "네이버 로그인 콜백을 하기 위한 메서드입니다.")
	public TokenDto naverCallback(@RequestParam String code, @RequestParam String state) throws IOException {
		TokenDto token = naverService.getNaverToken("token", code);
		naverService.saveNaverUser(naverService.getNaverUserByToken(token));
		return token;
	}

	@GetMapping("/sign-in/kakao")
	@Operation(summary = "카카오 로그인 메서드", description = "카카오 로그인을 하기 위한 메서드입니다.")
	public void kakaoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(kakaoService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/kakao/callback")
	@Operation(summary = "카카오 로그인 콜백 메서드", description = "카카오 로그인 콜백을 하기 위한 메서드입니다.")
	public TokenDto kakaoCallback(@RequestParam String code) throws IOException {
		TokenDto kakaoTokenDto = kakaoService.getToken(code);
		KakaoDto kakaoDto = kakaoService.getUserInfo(kakaoTokenDto);
		TokenDto tokenDto = kakaoService.signIn(kakaoDto);
		return tokenDto;
	}

	@GetMapping("/sign-in/google")
	@Operation(summary = "구글 로그인 콜백 메서드", description = "구글 로그인 후 리다이렉트 되어 인가 코드를 출력하는 메서드입니다.")
	public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(googleService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/google/callback")
	@Operation(summary = "구글 로그인 메서드", description = "구글로부터 사용자 정보를 얻어와 회원가입 및 로그인을 하기 위한 메서드입니다.")
	public TokenDto signIn(@RequestParam String code) throws IOException {
		TokenDto googleTokenDto = googleService.getToken(code);
		GoogleDto googleDto = googleService.getUserInfo(googleTokenDto);
		TokenDto tokenDto = googleService.signIn(googleDto);
		return tokenDto;
	}
  
  @GetMapping("/sign-in/apple")
	@Operation(summary = "애플 로그인 메서드", description = "애플 로그인을 하기 위한 메서드입니다.")
	public void appleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(appleService.getAuthorizationUrl());
	}

	@PostMapping("/sign-in/apple/callback")
	@Operation(summary = "애플 로그인 콜백 메서드", description = "애플 로그인 콜백을 하기 위한 메서드입니다.")
	public TokenDto appleCallback(@RequestBody String code) throws IOException, ParseException, JOSEException {
		String keyPath = appleService.readKeyPath();
		PrivateKey privateKey = appleService.craetePrivateKey(keyPath);
		String clientSecretKey = appleService.createClientSecretKey(privateKey);
		String idToken = appleService.getIdToken(code, clientSecretKey);
		return appleService.getToken(idToken);
}
