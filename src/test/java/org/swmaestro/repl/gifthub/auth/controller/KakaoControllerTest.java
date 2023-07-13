package org.swmaestro.repl.gifthub.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.service.KakaoService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class KakaoControllerTest {
	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private KakaoService kakaoService;

	@Test
	public void callbackTest() throws Exception {
		mockMvc.perform(get("/auth/kakao/callback")
						.param("code", "myawesomecode"))
				.andExpect(status().isOk());
	}

	@Test
	public void signInTest() throws Exception {
		String code = "myawesomecode";

		TokenDto kakaoTokenDto = TokenDto.builder()
				.accessToken("myawesomeKakaojwt")
				.refreshToken("myawesomeKakaojwt")
				.build();

		TokenDto tokenDto = TokenDto.builder()
				.accessToken("myawesomejwt")
				.refreshToken("myawesomejwt")
				.build();

		KakaoDto kakaoDto = KakaoDto.builder()
				.nickname("정인희")
				.username("dls@gmail.com")
				.build();

		when(kakaoService.getToken(code)).thenReturn(kakaoTokenDto);
		when(kakaoService.getUserInfo(kakaoTokenDto)).thenReturn(kakaoDto);
		when(kakaoService.signIn(kakaoDto)).thenReturn(tokenDto);

		mockMvc.perform(post("/auth/kakao/sign-in")
						.header("Authorization", "Bearer " + code))
				.andExpect(status().isOk());
	}


}
