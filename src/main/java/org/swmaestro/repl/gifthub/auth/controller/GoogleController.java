package org.swmaestro.repl.gifthub.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.GoogleService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "구글 로그인을 이용한 사용자 인증 관련 API")
public class GoogleController {
	private final GoogleService googleService;

	@GetMapping("/google/callback")
	@Operation(summary = "구글 로그인 콜백 메서드", description = "구글 로그인 후 리다이렉트 되어 인가 코드를 출력하는 메서드입니다.")
	public String callback(@RequestParam String code) {
		return code;
	}

	@PostMapping("/google/sign-in")
	@Operation(summary = "구글 로그인 메서드", description = "구글로부터 사용자 정보를 얻어와 회원가입 및 로그인을 하기 위한 메서드입니다.")
	public TokenDto signIn(@RequestHeader("Authorization") String code) {
		code = code.substring(7);
		TokenDto googleTokenDto = googleService.getToken(code);
		GoogleDto googleDto = googleService.getUserInfo(googleTokenDto);
		TokenDto tokenDto = googleService.signIn(googleDto);
		return tokenDto;
	}
}
