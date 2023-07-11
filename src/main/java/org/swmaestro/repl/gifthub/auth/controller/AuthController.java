package org.swmaestro.repl.gifthub.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final MemberService memberService;
	private final AuthService authService;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;

	@PostMapping("/auth/sign-up")
	public TokenDto signUp(@RequestBody SignUpDto signUpDto) {
		TokenDto tokenDto = memberService.create(signUpDto);
		return tokenDto;
	}

	@PostMapping("/auth/sign-in")
	public String signIn(@RequestBody SignInDto loginDto) {
		return authService.signIn(loginDto).getUsername();
	}
}
