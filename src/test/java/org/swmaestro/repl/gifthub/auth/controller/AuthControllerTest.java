package org.swmaestro.repl.gifthub.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.UserDeviceDto;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.DeviceService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@MockBean
	private DeviceService deviceService;

	@MockBean
	private JwtProvider jwtProvider;

	@Test
	public void signUpTest() throws Exception {
		SignUpDto signUpDto = SignUpDto.builder()
				.username("jinlee1703")
				.password("abc123##")
				.nickname("이진우")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		// when
		when(authService.signUp(any(SignUpDto.class))).thenReturn(jwtTokenDto);

		// then
		mockMvc.perform(post("/auth/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signUpDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void signInTest() throws Exception {
		SignInDto loginDto = SignInDto.builder()
				.username("jinlee1703")
				.password("abc123##")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		when(authService.signIn(any(SignInDto.class))).thenReturn(jwtTokenDto);

		mockMvc.perform(post("/auth/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void reissueAccessTokenTest() throws Exception {
		//given
		String refreshToken = "유효한 Refresh Token";
		String newAccessToken = "sampleNewAccessToken";
		String username = "jinlee1703";

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(refreshToken)
				.build();

		when(deviceService.createNewAccessTokenByValidateRefreshToken(refreshToken)).thenReturn(newAccessToken);
		when(jwtProvider.getUsername(refreshToken)).thenReturn(username);

		mockMvc.perform(post("/auth/refresh")
						.header("Authorization", refreshToken))
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void kakaoSignInTest() throws Exception {
		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("myawesomeKakaojwt")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("my.awesome.access-token")
				.refreshToken("my.awesome.refresh-token")
				.build();

		OAuthUserInfoDto oAuthUserInfoDto = OAuthUserInfoDto.builder()
				.id("123456789")
				.email("jinlee@kakao.com")
				.nickname("이진우")
				.build();

		// when
		when(authService.signIn(any(OAuthTokenDto.class), any(OAuthPlatform.class))).thenReturn(jwtTokenDto);

		mockMvc.perform(post("/auth/sign-in/kakao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(oAuthTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void googleSignInTest() throws Exception {
		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("myawesomeKakaojwt")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		// when
		when(authService.signIn(any(OAuthTokenDto.class), any(OAuthPlatform.class))).thenReturn(jwtTokenDto);

		mockMvc.perform(post("/auth/sign-in/google")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(oAuthTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void naverSignInTest() throws Exception {
		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("myawesomeKakaojwt")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		// when
		when(authService.signIn(any(OAuthTokenDto.class), any(OAuthPlatform.class))).thenReturn(jwtTokenDto);

		mockMvc.perform(post("/auth/sign-in/naver")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(oAuthTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void appleSignInTest() throws Exception {
		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("myawesomeKakaojwt")
				.build();

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		// when
		when(authService.signIn(any(OAuthTokenDto.class), any(OAuthPlatform.class))).thenReturn(jwtTokenDto);

		mockMvc.perform(post("/auth/sign-in/apple")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(oAuthTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	public void signOutTest() throws Exception {
		String username = "jinlee1703";
		String accessToken = "my_awesome_access_token";

		SignOutDto signOutDto = SignOutDto.builder()
				.deviceToken("my_awesome_device_token")
				.build();

		when(jwtProvider.getUsername(accessToken)).thenReturn(username);
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);

		mockMvc.perform(post("/auth/sign-out")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(signOutDto)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "test", roles = "ANONYMOUS")
	void signUpAnonymous() throws Exception {
		// given
		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();
		UserDeviceDto userDeviceDto = UserDeviceDto.builder()
				.deviceToken("device_token")
				.build();
		// when
		when(authService.signUpAnonymous(userDeviceDto)).thenReturn(jwtTokenDto);

		// then
		mockMvc.perform(post("/auth/sign-up/anonymous")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(userDeviceDto)))
				.andExpect(status().isOk());
	}
}
