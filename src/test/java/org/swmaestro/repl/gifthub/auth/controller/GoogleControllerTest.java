package org.swmaestro.repl.gifthub.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.GoogleService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GoogleControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private GoogleService googleService;

	@Test
	public void callbackTest() throws Exception {
		mockMvc.perform(get("/auth/google/callback")
						.param("code", "myawesomecode"))
				.andExpect(status().isOk());
	}

	@Test
	public void signInTest() throws Exception {
		String code = "myawesomecode";

		TokenDto googleTokenDto = TokenDto.builder()
				.accessToken("myawesomeKakaojwt")
				.refreshToken("myawesomeKakaojwt")
				.build();

		TokenDto tokenDto = TokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		GoogleDto googleDto = GoogleDto.builder()
				.nickname("정인희")
				.username("dls@gmail.com")
				.build();

		when(googleService.getToken(code)).thenReturn(googleTokenDto);
		when(googleService.getUserInfo(googleTokenDto)).thenReturn(googleDto);
		when(googleService.signIn(googleDto)).thenReturn(tokenDto);

		mockMvc.perform(post("/auth/google/sign-in")
						.header("Authorization", "Bearer " + code))
				.andExpect(status().isOk());
	}
}
