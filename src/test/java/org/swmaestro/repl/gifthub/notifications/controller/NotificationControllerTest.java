package org.swmaestro.repl.gifthub.notifications.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.notifications.dto.DeviceTokenSaveRequestDto;
import org.swmaestro.repl.gifthub.notifications.dto.NotificationReadResponseDto;
import org.swmaestro.repl.gifthub.notifications.service.NotificationService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private JwtProvider jwtProvider;

	@MockBean
	private NotificationService notificationService;

	/**
	 * 알림 목록 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void listNotificationTest() throws Exception {
		// given
		String accessToken = "myAccessToken";
		String username = "이진우";
		List<NotificationReadResponseDto> notifications = new ArrayList<>();
		notifications.add(NotificationReadResponseDto.builder()
				.id(1L)
				.type("유효기간 임박 알림")
				.message("유효기간이 3일 남았습니다.")
				.voucherId(1L)
				.notifiedAt(LocalDateTime.now())
				.build());
		// when
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(notificationService.list(username)).thenReturn(notifications);
		// then
		mockMvc.perform(get("/notifications").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].id").value(1L))
				.andExpect(jsonPath("$.data[0].type").value("유효기간 임박 알림"))
				.andExpect(jsonPath("$.data[0].message").value("유효기간이 3일 남았습니다."))
				.andReturn();

	}

	/**
	 * 디바이스 토큰 등록 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void saveDeviceToken() throws Exception {
		// given
		String accessToken = "my.access.token";
		String username = "이진우";
		DeviceTokenSaveRequestDto deviceTokenSaveRequestDto = DeviceTokenSaveRequestDto.builder().token("my.device.token").build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(notificationService.saveDeviceToken(username, deviceTokenSaveRequestDto.getToken())).thenReturn(true);

		// then
		mockMvc.perform(post("/notifications/device").header("Authorization", "Bearer " + accessToken)
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(deviceTokenSaveRequestDto))).andExpect(status().isOk()).andReturn();
	}

	/**
	 * 알림 상세 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readNotificationTest() throws Exception {
		String accessToken = "myAccessToken";
		String username = "이진우";
		NotificationReadResponseDto notificationReadResponseDto = NotificationReadResponseDto.builder()
				.id(1L)
				.type("유효기간 임박 알림")
				.message("유효기간이 3일 남았습니다.")
				.voucherId(1L)
				.notifiedAt(LocalDateTime.now())
				.checkedAt(LocalDateTime.now())
				.build();
		// when
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(notificationService.read(1L, username)).thenReturn(notificationReadResponseDto);
		// then
		mockMvc.perform(get("/notifications/1").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andReturn();
	}
}
