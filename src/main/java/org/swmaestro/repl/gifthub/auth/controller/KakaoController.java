package org.swmaestro.repl.gifthub.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.KakaoService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "카카오 로그인을 이용한 사용자 인증 관련 API")
public class KakaoController {
	private final KakaoService kakaoService;

	@GetMapping("/kakao/callback")
	@Operation(summary = "카카오 로그인 콜백 메서드", description = "카카오 로그인 후 리다이렉트 되어 인가 코드를 출력하는 메서드입니다.")
	public String callback(@RequestParam String code) {
		return code;
	}

	@PostMapping("/kakao/sign-in")
	@Operation(summary = "카카오 로그인 메서드", description = "카카오로부터 사용자 정보를 얻어와 회원가입 및 로그인을 하기 위한 메서드입니다.")
	public TokenDto signIn(@RequestHeader("Authorization") String code) {
		code = code.substring(7);
		TokenDto kakaoTokenDto = kakaoService.getToken(code);
		KakaoDto kakaoDto = kakaoService.getUserInfo(kakaoTokenDto);
		TokenDto tokenDto = kakaoService.signIn(kakaoDto);
		return tokenDto;
	}
}
