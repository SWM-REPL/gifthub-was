package org.swmaestro.repl.gifthub.notifications.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.notifications.dto.DeviceTokenRequestDto;
import org.swmaestro.repl.gifthub.notifications.dto.NoticeNotificationDto;
import org.swmaestro.repl.gifthub.notifications.dto.NotificationReadResponseDto;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 관련 API")
public class NotificationController {
	private final NotificationService notificationService;
	private final FCMNotificationService fcmNotificationService;
	private final UserService userService;
	private final JwtProvider jwtProvider;

	@GetMapping
	@Operation(summary = "Notification 목록 조회 메서드", description = "클라이언트에서 요청한 알림 목록 정보를 조회하기 위한 메서드입니다. 응답으로 알림 type, message, notified date, 기프티콘 정보, 알림 확인 시간을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
			@ApiResponse(responseCode = "400(401)", description = "유효하지 않은 토큰"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원"),
	})
	public ResponseEntity<Message> listNotification(HttpServletRequest request) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		List<NotificationReadResponseDto> notificationList = notificationService.list(username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(notificationList)
						.build());
	}

	@PostMapping("/device")
	@Operation(summary = "디바이스 토큰 등록 메서드", description = "알림 서비스를 위한 디바이스 토큰을 등록하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "디바이스 토큰 등록 성공"),
			@ApiResponse(responseCode = "400(400)", description = "존재하지 토큰 등록 시도"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원"),
	})
	public ResponseEntity<Message> registerDeviceToken(
			HttpServletRequest request,
			@RequestHeader("Authorization") String accessToken,
			@RequestBody DeviceTokenRequestDto deviceTokenRequestDto) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		notificationService.saveDeviceToken(username, deviceTokenRequestDto.getToken());
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@DeleteMapping("/device")
	@Operation(summary = "디바이스 토큰 삭제 메서드", description = "알림 서비스를 위한 디바이스 토큰을 삭제하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "디바이스 토큰 등록 성공"),
			@ApiResponse(responseCode = "400(400)", description = "존재하지 토큰 등록 시도"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원"),
	})
	public ResponseEntity<Message> deleteDeviceToken(
			HttpServletRequest request,
			@RequestHeader("Authorization") String accessToken,
			@RequestBody DeviceTokenRequestDto deviceTokenRequestDto) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		User user = userService.read(username);
		notificationService.deleteDeviceToken(user, deviceTokenRequestDto.getToken());
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@GetMapping("/{notificationId}")
	@Operation(summary = "Notification 상세 조회 메서드", description = "클라이언트에서 요청한 알림 상세 정보를 조회하기 위한 메서드입니다. 응답으로 알림 type, message, notified date, 기프티콘 정보, 기존 알림 확인 시간을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "알림 상세 조회 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 알림"),
			@ApiResponse(responseCode = "400(403)", description = "알림 상세 조회 권한 없음"),
	})
	public ResponseEntity<Message> readNotification(
			HttpServletRequest request,
			@RequestHeader("Authorization") String accessToken,
			@PathVariable Long notificationId) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		NotificationReadResponseDto notificationReadResponseDto = notificationService.read(notificationId, username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(notificationReadResponseDto)
						.build());
	}

	@PostMapping
	@Operation(summary = "Notification 전송 메서드", description = "모든 클라이언트에게 일괄적으로 알림을 전송하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "알림 전송 성공")
	})
	public ResponseEntity<Message> sendNotification(
			HttpServletRequest request,
			@RequestBody NoticeNotificationDto noticeNotificationDto) {
		fcmNotificationService.sendNotification(noticeNotificationDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@PostMapping("/expiration/allow")
	@Operation(summary = "만료 알림 수신 허용 메서드", description = "만료 알림 수신을 허용하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "만료 알림 수신 허용 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원")
	})
	public ResponseEntity<Message> allowExpirationNotification(
			HttpServletRequest request,
			@RequestHeader("Authorization") String accessToken) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		userService.allowNotifications(username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@PostMapping("/expiration/deny")
	@Operation(summary = "만료 알림 수신 거부 메서드", description = "만료 알림 수신을 거부하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "만료 알림 수신 거부 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원")
	})
	public ResponseEntity<Message> denyExpirationNotification(
			HttpServletRequest request,
			@RequestHeader("Authorization") String accessToken) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		userService.denyNotifications(username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}
}
