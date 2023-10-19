package org.swmaestro.repl.gifthub.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.UserDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserInfoResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.UserUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.service.UserService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void deleteMember() throws Exception {
		// given
		UserDeleteResponseDto userDeleteResponseDto = UserDeleteResponseDto.builder()
				.id(1L)
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(userService.delete(1L)).thenReturn(userDeleteResponseDto);

		// then
		mockMvc.perform(delete("/users/1")
						.header("Authorization", "Bearer my_awesome_access_token"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void updateMember() throws Exception {
		//given
		String username = "이진우";
		Long userId = 1L;
		UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
				.nickname("이진우11")
				.password("1234")
				.build();
		UserUpdateResponseDto userUpdateResponseDto = UserUpdateResponseDto.builder()
				.id(1L)
				.nickname("이진우11")
				.build();
		//when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(userService.update(anyString(), anyLong(), any(UserUpdateRequestDto.class))).thenReturn(userUpdateResponseDto);

		//then
		mockMvc.perform(patch("/users/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userUpdateResponseDto)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readMember() throws Exception {
		//given
		String username = "이진우";
		Long userId = 1L;
		UserReadResponseDto userReadResponseDto = UserReadResponseDto.builder()
				.id(1L)
				.nickname("이진우")
				.build();
		//when
		when(userService.read(anyLong())).thenReturn(userReadResponseDto);

		//then
		mockMvc.perform(get("/users/1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void createOAuthInfo() throws Exception {
		// given
		User user = User.builder()
				.username("my_username")
				.nickname("my_nickname")
				.password("my_password")
				.build();

		OAuth oAuth = OAuth.builder()
				.platform(OAuthPlatform.NAVER)
				.platformId("my_naver_unique_id")
				.user(user)
				.email("my_naver_email")
				.nickname("my_naver_nickname")
				.build();

		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("my_oauth_token")
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(userService.createOAuthInfo(any(User.class), any(OAuthPlatform.class), any(OAuthTokenDto.class))).thenReturn(oAuth);

		// then
		mockMvc.perform(post("/users/oauth/naver")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(oAuthTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void deleteOAuthInfo() throws Exception {
		// given
		User user = User.builder()
				.username("my_username")
				.nickname("my_nickname")
				.password("my_password")
				.build();

		OAuth oAuth = OAuth.builder()
				.platform(OAuthPlatform.NAVER)
				.platformId("my_naver_unique_id")
				.user(user)
				.email("my_naver_email")
				.nickname("my_naver_nickname")
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(userService.deleteOAuthInfo(any(User.class), any(OAuthPlatform.class))).thenReturn(oAuth);

		// then
		mockMvc.perform(delete("/users/oauth/naver")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readMyInfo() throws Exception {
		// given
		User user = User.builder()
				.username("my_username")
				.nickname("my_nickname")
				.password("my_password")
				.build();
		List<OAuthUserInfoDto> oAuth = List.of(OAuthUserInfoDto.builder()
				.id("my_naver_unique_id")
				.email("my_naver_email")
				.nickname("my_naver_nickname")
				.Provider("NAVER")
				.build());
		UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.builder()
				.username("my_username")
				.nickname("my_nickname")
				.oauth(oAuth)
				.build();
		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(userService.readInfo(anyString())).thenReturn(userInfoResponseDto);

		// then
		mockMvc.perform(get("/users/me")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}