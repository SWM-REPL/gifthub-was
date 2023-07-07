package org.swmaestro.repl.gifthub.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;

@RestController
@RequiredArgsConstructor
public class AuthController {
	private final MemberService memberService;

	@PostMapping("/auth/sign-up")
	public String signUp(@RequestBody SignUpDto signUpDto) {
		return memberService.create(signUpDto);
	}
}
