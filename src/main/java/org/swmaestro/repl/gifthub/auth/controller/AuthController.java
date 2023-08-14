package org.swmaestro.repl.gifthub.auth.controller;

import java.io.IOException;
import java.security.PrivateKey;
import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.NaverDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.service.AppleService;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.GoogleService;
import org.swmaestro.repl.gifthub.auth.service.KakaoService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.service.NaverService;
import org.swmaestro.repl.gifthub.auth.service.OAuthService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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
	private final OAuthService oAuthService;

	@PostMapping("/sign-up")
	@Operation(summary = "회원가입 메서드", description = "사용자가 회원가입을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> signUp(@RequestBody SignUpDto signUpDto) {
		TokenDto tokenDto = memberService.create(signUpDto);
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("성공적으로 회원가입되었습니다!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PostMapping("/sign-in")
	@Operation(summary = "로그인 메서드", description = "사용자가 로그인을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> signIn(@RequestBody SignInDto loginDto) {
		TokenDto tokenDto = authService.signIn(loginDto);
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("로그인 성공!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh Token을 이용한 New RefreshToken, New Access Token 발급 메서드", description = "Refresh Token을 이용하여 새로운 Refresh Token, Access Token을 발급 받기 위한 메서드입니다.")
	public ResponseEntity<Message> reissueAccessToken(@RequestHeader("Authorization") String refreshToken) {
		String newAccessToken = refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken);
		String newRefreshToken = refreshTokenService.createNewRefreshTokenByValidateRefreshToken(refreshToken);

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();

		refreshToken = refreshToken.substring(7);
		refreshTokenService.storeRefreshToken(tokenDto, jwtProvider.getUsername(refreshToken));

		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("새로운 Access Token, Refresh Token이 발급되었습니다!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping("/sign-in/naver")
	@Operation(summary = "네이버 로그인 메서드", description = "네이버 로그인을 하기 위한 메서드입니다.")
	public void naverLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(naverService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/naver/callback")
	@Operation(summary = "네이버 로그인 콜백 메서드", description = "네이버 로그인 콜백을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> naverCallback(@RequestParam String code, @RequestParam String state) throws IOException {
		TokenDto token = naverService.getNaverToken("token", code);
		NaverDto naverDto = naverService.getUserInfo(token);
		Member member = naverService.signUp(naverDto);
		oAuthService.save(member, OAuthPlatform.NAVER, naverDto.getId());
		TokenDto tokenDto = naverService.signIn(naverDto);
		return new ResponseEntity<Message>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("네이버 로그인 성공!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping("/sign-in/kakao")
	@Operation(summary = "카카오 로그인 메서드", description = "카카오 로그인을 하기 위한 메서드입니다.")
	public void kakaoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(kakaoService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/kakao/callback")
	@Operation(summary = "카카오 로그인 콜백 메서드", description = "카카오 로그인 콜백을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> kakaoCallback(@RequestParam String code) throws IOException {
		TokenDto kakaoTokenDto = kakaoService.getToken(code);
		KakaoDto kakaoDto = kakaoService.getUserInfo(kakaoTokenDto);
		Member member = memberService.read(kakaoDto.getUsername());
		oAuthService.save(member, OAuthPlatform.KAKAO, kakaoDto.getId());
		TokenDto tokenDto = kakaoService.signIn(kakaoDto);
		return new ResponseEntity<Message>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("카카오 로그인 성공!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping("/sign-in/google")
	@Operation(summary = "구글 로그인 콜백 메서드", description = "구글 로그인 후 리다이렉트 되어 인가 코드를 출력하는 메서드입니다.")
	public void googleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(googleService.getAuthorizationUrl());
	}

	@GetMapping("/sign-in/google/callback")
	@Operation(summary = "구글 로그인 메서드", description = "구글로부터 사용자 정보를 얻어와 회원가입 및 로그인을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> signIn(@RequestParam String code) throws IOException {
		TokenDto googleTokenDto = googleService.getToken(code);
		GoogleDto googleDto = googleService.getUserInfo(googleTokenDto);
		TokenDto tokenDto = googleService.signIn(googleDto);
		return new ResponseEntity<Message>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("구글 로그인 성공!")
						.data(tokenDto)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping("/sign-in/apple")
	@Operation(summary = "애플 로그인 메서드", description = "애플 로그인을 하기 위한 메서드입니다.")
	public void appleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendRedirect(appleService.getAuthorizationUrl());
	}

	@PostMapping("/sign-in/apple/callback")
	@Operation(summary = "애플 로그인 콜백 메서드", description = "애플 로그인 콜백을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> appleCallback(@RequestBody String code) throws IOException, ParseException, JOSEException {
		String keyPath = appleService.readKeyPath();
		PrivateKey privateKey = appleService.craetePrivateKey(keyPath);
		String clientSecretKey = appleService.createClientSecretKey(privateKey);
		String idToken = appleService.getIdToken(code, clientSecretKey);
		return new ResponseEntity<Message>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("애플 로그인 성공!")
						.data(idToken)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PostMapping("/sign-out")
	@Operation(summary = "로그아웃 메서드", description = "사용자가 로그아웃을 하기 위한 메서드입니다.")
	public ResponseEntity<Message> signOut(HttpServletRequest request) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		authService.signOut(username);
		return new ResponseEntity<Message>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("로그아웃 성공!")
						.data(null)
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}
}
