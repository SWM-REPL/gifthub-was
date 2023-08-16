package org.swmaestro.repl.gifthub.auth.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.PrivateKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.AppleDto;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.NaverDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.service.AppleService;
import org.swmaestro.repl.gifthub.auth.service.AuthService;
import org.swmaestro.repl.gifthub.auth.service.GoogleService;
import org.swmaestro.repl.gifthub.auth.service.KakaoService;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.service.NaverService;
import org.swmaestro.repl.gifthub.auth.service.OAuthService;
import org.swmaestro.repl.gifthub.auth.service.RefreshTokenService;
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

	@MockBean
	private OAuthService oAuthService;

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

		// when
		when(memberService.create(signUpDto)).thenReturn(tokenDto);

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
	public void reissueAccessTokenTest() throws Exception {
		//given
		String refreshToken = "유효한 Refresh Token";
		String newAccessToken = "sampleNewAccessToken";
		String username = "jinlee1703";

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(newAccessToken)
				.refreshToken(refreshToken)
				.build();

		when(refreshTokenService.createNewAccessTokenByValidateRefreshToken(refreshToken)).thenReturn(newAccessToken);
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

		Member member = Member.builder()
				.username(kakaoDto.getUsername())
				.nickname(kakaoDto.getNickname())
				.build();

		when(kakaoService.getToken(code)).thenReturn(kakaoTokenDto);
		when(kakaoService.getUserInfo(kakaoTokenDto)).thenReturn(kakaoDto);
		when(memberService.read(kakaoDto.getUsername())).thenReturn(member);

		when(kakaoService.signIn(kakaoDto)).thenReturn(tokenDto);

		mockMvc.perform(get("/auth/sign-in/kakao/callback")
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

		mockMvc.perform(get("/auth/sign-in/google/callback")
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
				.id(1L)
				.build();
		NaverDto naverDto = NaverDto.builder()
				.email(member.getUsername())
				.nickname(member.getNickname())
				.build();

		when(naverService.getNaverToken("token", code)).thenReturn(token);
		when(naverService.getUserInfo(token)).thenReturn(naverDto);
		when(naverService.signUp(naverDto)).thenReturn(member);
		when(naverService.signIn(naverDto, 1L)).thenReturn(token);

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

		AppleDto appleDto = AppleDto.builder()
				.email("binarywooo@gmail.com")
				.nickname("이진우")
				.build();

		when(appleService.readKeyPath()).thenReturn(keyPath);
		when(appleService.craetePrivateKey(keyPath)).thenReturn(privateKey);
		when(appleService.createClientSecretKey(privateKey)).thenReturn(clientSecretKey);
		when(appleService.getIdToken(clientSecretKey, "my_awesome_code")).thenReturn(idToken);
		when(appleService.getUserInfo(idToken)).thenReturn(appleDto);

		mockMvc.perform(post("/auth/sign-in/apple/callback")
						.requestAttr("code", "my_awesome_code")
						.header("Authorization", "Bearer " + accesstoken))
				.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	public void signOutTest() throws Exception {
		String username = "jinlee1703";
		String accessToken = "my_awesome_access_token";

		when(jwtProvider.getUsername(accessToken)).thenReturn(username);
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);

		mockMvc.perform(post("/auth/sign-out")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

	}
}
