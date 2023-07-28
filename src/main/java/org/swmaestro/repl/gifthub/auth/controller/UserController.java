package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "사용자 관련 API")
public class UserController {
	private final MemberService memberService;

	@DeleteMapping("/{userId}")
	@Operation(summary = "User 삭제 메서드", description = "클라이언트에서 요청한 사용자 정보를 삭제(Soft-Delete)하기 위한 메서드입니다.")
	public MemberDeleteResponseDto deleteMember(@PathVariable Long userId) {
		return memberService.delete(userId);
	}
}
