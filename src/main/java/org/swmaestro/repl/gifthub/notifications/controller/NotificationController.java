package org.swmaestro.repl.gifthub.notifications.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.notifications.dto.DeviceTokenSaveRequestDto;
import org.swmaestro.repl.gifthub.notifications.service.FCMNotificationService;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "알림 관련 API")
public class NotificationController {
	private final NotificationService notificationService;
	private final FCMNotificationService fcmNotificationService;
	private final JwtProvider jwtProvider;

	@GetMapping
	@Operation(summary = "Notification 목록 조회 메서드", description = "클라이언트에서 요청한 알림 목록 정보를 조회하기 위한 메서드입니다. 응답으로 알림 type, message, notified date, 기프티콘 정보를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원"),
	})
	public ResponseEntity<Message> listNotification(@RequestHeader("Authorization") String accessToken) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("알림 목록 조회에 성공하였습니다!")
						.data(notificationService.list(username))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PostMapping("/device")
	@Operation(summary = "디바이스 토큰 등록 메서드", description = "알림 서비스를 위한 디바이스 토큰을 등록하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "디바이스 토큰 등록 성공"),
			@ApiResponse(responseCode = "400(400)", description = "존재하지 토큰 등록 시도"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 회원"),
	})
	public ResponseEntity<Message> registerDeviceToken(
			@RequestHeader("Authorization") String accessToken,
			@RequestBody DeviceTokenSaveRequestDto deviceTokenSaveRequestDto) {
		String username = jwtProvider.getUsername(accessToken.substring(7));
		notificationService.saveDeviceToken(username, deviceTokenSaveRequestDto.getToken());
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("디바이스 토큰 등록에 성공하였습니다!")
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}
}
