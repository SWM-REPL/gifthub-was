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
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.UserService;
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

	@MockBean
	private UserService userService;

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

	/**
	 * 만료 알림 수신 허용 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void allowNotifications() throws Exception {
		String accessToken = "my.access.token";
		String username = "이진우";
		User user = User.builder().username(username).build();

		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(userService.read(username)).thenReturn(user);

		mockMvc.perform(post("/notifications/expiration/allow").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());
	}

	/**
	 * 만료 알림 수신 거부 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void denyNotifications() throws Exception {
		String accessToken = "my.access.token";
		String username = "이진우";
		User user = User.builder().username(username).build();

		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(userService.read(username)).thenReturn(user);

		mockMvc.perform(post("/notifications/expiration/deny").header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());
	}

}
