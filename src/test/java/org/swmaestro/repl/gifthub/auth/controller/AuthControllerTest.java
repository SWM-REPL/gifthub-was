package org.swmaestro.repl.gifthub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private MemberService memberService;

	@MockBean
	private AuthService authService;

	@MockBean
	private RefreshTokenService refreshTokenService;

	@MockBean
	private JwtProvider jwtProvider;

	@Test
	public void signUpTest() throws Exception {
		SignUpDto signUpDto = SignUpDto.builder()
				.username("jinlee1703")
				.password("abc123##")
				.nickname("이진우")
				.build();

		TokenDto tokenDto = TokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();


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

		TokenDto tokenDto = TokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		when(authService.signIn(any(SignInDto.class))).thenReturn(tokenDto);

		mockMvc.perform(post("/auth/sign-up")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(loginDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void validateRefreshTokenTest() throws Exception {
		String refreshToken = "sampleRefreshToken";
		String newAccessToken = "sampleNewAccessToken";
		String newRefreshToken = "sampleNewRefreshToken";
		String username = "jinlee1703";

		when(refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken)).thenReturn(newAccessToken);
		when(refreshTokenService.createNewRefreshTokenByValidateRefreshToken(refreshToken)).thenReturn(newRefreshToken);
		when(jwtProvider.getUsername(refreshToken)).thenReturn(username);

		mockMvc.perform(post("/auth/refresh")
						.header("Authorization", refreshToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").value(newAccessToken))
				.andExpect(jsonPath("$.refreshToken").value(newRefreshToken));
	}
}
