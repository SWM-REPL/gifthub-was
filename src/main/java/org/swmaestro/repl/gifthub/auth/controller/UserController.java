package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
			@ApiResponse(responseCode = "400(404-1)", description = "존재하지 않는 회원 아이디 입력"),
			@ApiResponse(responseCode = "400(404-2)", description = "이미 삭제된 회원 아이디 입력")
	})
	public ResponseEntity<Message> deleteMember(@PathVariable Long userId) {
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("성공적으로 삭제되었습니다!")
						.data(memberService.delete(userId))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PatchMapping("/{userId}")
	@Operation(summary = "User 정보 수정 메서드", description = "클라이언트에서 요청한 사용자 정보를 수정하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 수정 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원 아이디 입력")
	})
	public ResponseEntity<Message> updateMember(HttpServletRequest request, @PathVariable Long userId,
			@RequestBody MemberUpdateRequestDto memberUpdateRequestDto) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("성공적으로 수정되었습니다!")
						.data(memberService.update(username, userId, memberUpdateRequestDto))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping("/{userId}")
	@Operation(summary = "User 정보 조회 메서드", description = "클라이언트에서 요청한 사용자 정보를 조회하기 위한 메서드입니다. 응답으로 회원 id와 nickname을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 조회 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원 아이디 입력")
	})
	public ResponseEntity<Message> readMember(@PathVariable Long userId) {
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("성공적으로 조회되었습니다!")
						.data(memberService.read(userId))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}
}
