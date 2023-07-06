package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;

@RestController
public class AuthController {
	private final MemberService memberService;

	@Autowired
	public AuthController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping("/auth/sign-up")
	public Long signUp(@RequestBody SignUpDto signUpDto) {
		return memberService.create(signUpDto);
	}
}
