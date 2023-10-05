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
import org.swmaestro.repl.gifthub.auth.dto.AppleDto;
import org.swmaestro.repl.gifthub.auth.dto.AppleTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.NaverDto;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
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
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void kakaoSignInTest() throws Exception {
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

		when(kakaoService.getUserInfo(kakaoTokenDto)).thenReturn(kakaoDto);
		when(memberService.read(kakaoDto.getUsername())).thenReturn(member);
		when(kakaoService.signIn(kakaoDto)).thenReturn(tokenDto);

		mockMvc.perform(post("/auth/sign-in/kakao")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(kakaoTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void googleSignInTest() throws Exception {

		TokenDto googleTokenDto = TokenDto.builder()
				.accessToken("my.awesome.google.jwt")
				.refreshToken("my.awesome.google.jwt")
				.build();

		GoogleDto googleDto = GoogleDto.builder()
				.nickname("정인희")
				.username("dls@gmail.com")
				.build();

		when(googleService.getUserInfo(googleTokenDto)).thenReturn(googleDto);
		when(googleService.signIn(googleDto)).thenReturn(googleTokenDto);

		mockMvc.perform(post("/auth/sign-in/google")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(googleTokenDto)))
				.andExpect(status().isOk());
	}

	@Test
	public void naverSignInTest() throws Exception {
		TokenDto token = TokenDto.builder()
				.accessToken("accesstoken")
				.refreshToken("refreshtoken")
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

		when(naverService.getUserInfo(token)).thenReturn(naverDto);
		when(naverService.signUp(naverDto)).thenReturn(member);
		when(naverService.signIn(naverDto, 1L)).thenReturn(token);

		mockMvc.perform(post("/auth/sign-in/naver")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(token)))
				.andExpect(status().isOk());
	}

	@Test
	public void appleSignInTest() throws Exception {
		String idToken = "my.awesome.id_token";

		AppleTokenDto appleTokenDto = AppleTokenDto.builder()
				.identityToken(idToken)
				.build();

		AppleDto appleDto = AppleDto.builder()
				.id("my_awesome_id")
				.email("binarywooo@gmail.com")
				.nickname("이진우")
				.build();

		Member member = Member.builder()
				.username("jinlee1703@naver.com")
				.nickname("이진우")
				.id(1L)
				.build();

		TokenDto token = TokenDto.builder()
				.accessToken("accesstoken")
				.refreshToken("refreshtoken")
				.build();

		when(appleService.getUserInfo(idToken)).thenReturn(appleDto);
		when(appleService.signUp(appleDto)).thenReturn(member);
		when(appleService.signIn(appleDto, 1L)).thenReturn(token);

		mockMvc.perform(post("/auth/sign-in/apple")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(appleTokenDto)))
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
}
