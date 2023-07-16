package org.swmaestro.repl.gifthub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.*;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.service.*;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import java.security.PrivateKey;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

	@MockBean
	private KakaoService kakaoService;

	@MockBean
	private GoogleService googleService;

	@MockBean
	private NaverService naverService;

	@MockBean
	private AppleService appleService;

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
		//given
		String refreshToken = "유효하지 않은 Refresh Token";
		String newAccessToken = "sampleNewAccessToken";
		String newRefreshToken = "sampleNewRefreshToken";
		String username = "jinlee1703";

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken)
				.build();

		when(refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken)).thenReturn(null);
		when(refreshTokenService.createNewRefreshTokenByValidateRefreshToken(refreshToken)).thenReturn(null);
		when(jwtProvider.getUsername(refreshToken)).thenReturn(username);


		mockMvc.perform(post("/auth/refresh")
						.header("Authorization", refreshToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void kakaoSignInCallbackTest() throws Exception {
		String accesstoken = "myawesome_accesstoken";
		String code = "myawesome_code";
		String state = "myawesome_state";

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

		when(kakaoService.getToken(code)).thenReturn(tokenDto);
		when(kakaoService.getUserInfo(tokenDto)).thenReturn(kakaoDto);
		when(kakaoService.signIn(kakaoDto)).thenReturn(tokenDto);

		mockMvc.perform(get("/auth/sign-in/naver/callback")
						.queryParam("code", code)
						.queryParam("state", state)
						.header("Authorization", "Bearer " + accesstoken))
				.andExpect(status().isOk());
	}

	@Test
	public void googleSignInCallbackTest() throws Exception {
		String accesstoken = "myawesome_accesstoken";
		String code = "myawesome_code";
		String state = "myawesome_state";

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

		when(googleService.getToken(code)).thenReturn(tokenDto);
		when(googleService.getUserInfo(tokenDto)).thenReturn(googleDto);
		when(googleService.signIn(googleDto)).thenReturn(tokenDto);

		mockMvc.perform(get("/auth/sign-in/naver/callback")
						.queryParam("code", code)
						.queryParam("state", state)
						.header("Authorization", "Bearer " + accesstoken))
				.andExpect(status().isOk());
	}

	@Test
	public void naverSignInCallbackTest() throws Exception {
		String accesstoken = "myawesome_accesstoken";
		String code = "myawesome_code";
		String state = "myawesome_state";
		TokenDto token = TokenDto.builder()
				.accessToken(accesstoken)
				.refreshToken(accesstoken)
				.build();
		Member member = Member.builder()
				.username("jinlee1703@naver.com")
				.nickname("이진우")
				.build();

		when(naverService.getNaverToken("token", code)).thenReturn(token);
		when(naverService.getNaverUserByToken(token)).thenReturn(member);

		mockMvc.perform(get("/auth/sign-in/naver/callback")
						.queryParam("code", code)
						.queryParam("state", state)
						.header("Authorization", "Bearer " + accesstoken))
				.andExpect(status().isOk());
	}

	@Test
	public void appleSignInCallbackTest() throws Exception {
		String keyPath = "my_awesome_key_path";
		String clientSecretKey = "my_awesome_client_secret_key";
		String idToken = "my_awesome_id_token";
		String accesstoken = "my_awesome_access_token";
		String refreshToken = "my_awesome_refresh_token";
		PrivateKey privateKey = mock(PrivateKey.class);

		TokenDto token = TokenDto.builder()
			.accessToken(accesstoken)
			.refreshToken(refreshToken)
			.build();

		when(appleService.readKeyPath()).thenReturn(keyPath);
		when(appleService.craetePrivateKey(keyPath)).thenReturn(privateKey);
		when(appleService.createClientSecretKey(privateKey)).thenReturn(clientSecretKey);
		when(appleService.getIdToken(clientSecretKey, "my_awesome_code")).thenReturn(idToken);
		when(appleService.getToken(idToken)).thenReturn(token);

		mockMvc.perform(post("/auth/sign-in/apple/callback")
				.requestAttr("code", "my_awesome_code")
				.header("Authorization", "Bearer " + accesstoken))
			.andExpect(status().isBadRequest());
	}
}
