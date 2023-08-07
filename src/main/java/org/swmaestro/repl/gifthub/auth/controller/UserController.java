package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "사용자 관련 API")
public class UserController {
	private final MemberService memberService;
	private final JwtProvider jwtProvider;

	@DeleteMapping("/{userId}")
	@Operation(summary = "User 삭제 메서드", description = "클라이언트에서 요청한 사용자 정보를 삭제(Soft-Delete)하기 위한 메서드입니다.")
	public MemberDeleteResponseDto deleteMember(@PathVariable Long userId) {
		return memberService.delete(userId);
	}

	@PatchMapping("/{userId}")
	@Operation(summary = "User 정보 수정 메서드", description = "클라이언트에서 요청한 사용자 정보를 수정하기 위한 메서드입니다.")
	public MemberUpdateResponseDto updateMember(HttpServletRequest request, @PathVariable Long userId,
			@RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return memberService.update(username, userId, memberUpdateRequestDto);
	}

	@GetMapping("/{userId}")
	@Operation(summary = "User 정보 조회 메서드", description = "클라이언트에서 요청한 사용자 정보를 조회하기 위한 메서드입니다. 응답으로 회원 id와 nickname을 반환합니다.")
	public MemberReadResponseDto readMember(@PathVariable Long userId) {
		return memberService.read(userId);
	}
}
