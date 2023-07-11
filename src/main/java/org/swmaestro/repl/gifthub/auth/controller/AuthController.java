package org.swmaestro.repl.gifthub.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final MemberService memberService;
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;

	@PostMapping("/sign-up")
	public TokenDto signUp(@RequestBody SignUpDto signUpDto) {
		TokenDto tokenDto = memberService.create(signUpDto);
		return tokenDto;
	}

	@PostMapping("/sign-in")
	public TokenDto signIn(@RequestBody SignInDto loginDto) {
		TokenDto tokenDto = authService.signIn(loginDto);
		return tokenDto;
	}

	@PostMapping("/refresh")
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
}
