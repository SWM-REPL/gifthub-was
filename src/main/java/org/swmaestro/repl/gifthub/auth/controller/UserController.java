package org.swmaestro.repl.gifthub.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.OAuthService;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.util.SuccessMessage;

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
	private final UserService userService;
	private final OAuthService oAuthService;
	private final JwtProvider jwtProvider;

	@DeleteMapping("/{userId}")
	@Operation(summary = "User 삭제 메서드", description = "클라이언트에서 요청한 사용자 정보를 삭제(Soft-Delete)하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 삭제 성공"),
			@ApiResponse(responseCode = "400(404-1)", description = "존재하지 않는 회원 아이디 입력"),
			@ApiResponse(responseCode = "400(404-2)", description = "이미 삭제된 회원 아이디 입력")
	})
	public ResponseEntity<Message> deleteMember(HttpServletRequest request, @PathVariable Long userId) {
		MemberDeleteResponseDto deletedMember = userService.delete(userId);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(deletedMember)
						.build());
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
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(userService.update(username, userId, memberUpdateRequestDto))
						.build());
	}

	@GetMapping("/{userId}")
	@Operation(summary = "User 정보 조회 메서드", description = "클라이언트에서 요청한 사용자 정보를 조회하기 위한 메서드입니다. 응답으로 회원 id와 nickname을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원 조회 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원 아이디 입력")
	})
	public ResponseEntity<Message> readMember(HttpServletRequest request, @PathVariable Long userId) {
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(userService.read(userId))
						.build());
	}

	@PostMapping("/oauth/{platform}")
	@Operation(summary = "OAuth 연동 계정 추가 메서드", description = "사용자의 기존 계정에 OAuth 계정을 추가 연동하기 위한 메서드입니다.\n 파라미터로는 'naver', 'kakao', 'google', 'apple'를 받을 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OAuth 연동 계정 추가 성공"),
			@ApiResponse(responseCode = "400", description = "OAuth 연동 계정 추가 실패")
	})
	public ResponseEntity<Message> createOAuthInfo(HttpServletRequest request, @PathVariable String platform,
			@RequestBody OAuthTokenDto oAuthTokenDto) {
		OAuthPlatform oAuthPlatform;

		switch (platform) {
			case "naver" -> oAuthPlatform = OAuthPlatform.NAVER;
			case "kakao" -> oAuthPlatform = OAuthPlatform.KAKAO;
			case "google" -> oAuthPlatform = OAuthPlatform.GOOGLE;
			case "apple" -> oAuthPlatform = OAuthPlatform.APPLE;
			default -> throw new BusinessException("올바르지 않은 플랫폼에 대한 요청입니다.", StatusEnum.BAD_REQUEST);
		}

		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		User user = userService.read(username);
		userService.createOAuthInfo(user, oAuthPlatform, oAuthTokenDto);
		return ResponseEntity.ok(SuccessMessage.builder()
				.path(request.getRequestURI())
				.build());
	}

	@DeleteMapping("/oauth/{platform}")
	@Operation(summary = "OAuth 연동 계정 삭제 메서드", description = "사용자의 기존 계정에 연동된 OAuth 계정을 삭제하기 위한 메서드입니다.\n 파라미터로는 'naver', 'kakao', 'google', 'apple'를 받을 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "OAuth 연동 계정 삭제 성공"),
			@ApiResponse(responseCode = "400", description = "OAuth 연동 계정 삭제 실패")
	})
	public ResponseEntity<Message> deleteOAuthInfo(HttpServletRequest request, @PathVariable String platform) {
		OAuthPlatform oAuthPlatform;

		switch (platform) {
			case "naver" -> oAuthPlatform = OAuthPlatform.NAVER;
			case "kakao" -> oAuthPlatform = OAuthPlatform.KAKAO;
			case "google" -> oAuthPlatform = OAuthPlatform.GOOGLE;
			case "apple" -> oAuthPlatform = OAuthPlatform.APPLE;
			default -> throw new BusinessException("올바르지 않은 플랫폼에 대한 요청입니다.", StatusEnum.BAD_REQUEST);
		}

		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		User user = userService.read(username);
		userService.deleteOAuthInfo(user, oAuthPlatform);
		return ResponseEntity.ok(SuccessMessage.builder()
				.path(request.getRequestURI())
				.build());
	}
}
