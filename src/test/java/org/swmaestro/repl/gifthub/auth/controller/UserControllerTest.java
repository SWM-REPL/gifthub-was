package org.swmaestro.repl.gifthub.auth.controller;

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
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberService memberService;

	@MockBean
	private JwtProvider jwtProvider;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void deleteMember() throws Exception {
		// given
		MemberDeleteResponseDto userDeleteResponseDto = MemberDeleteResponseDto.builder()
				.id(1L)
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(memberService.delete(1L)).thenReturn(userDeleteResponseDto);

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
		MemberUpdateRequestDto memberUpdateRequestDto = MemberUpdateRequestDto.builder()
				.nickname("이진우11")
				.password("1234")
				.build();
		MemberUpdateResponseDto memberUpdateResponseDto = MemberUpdateResponseDto.builder()
				.id(1L)
				.nickname("이진우11")
				.build();
		//when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(memberService.update(anyString(), anyLong(), any(MemberUpdateRequestDto.class))).thenReturn(memberUpdateResponseDto);

		//then
		mockMvc.perform(patch("/users/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(memberUpdateResponseDto)))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readMember() throws Exception {
		//given
		String username = "이진우";
		Long userId = 1L;
		MemberReadResponseDto memberReadResponseDto = MemberReadResponseDto.builder()
				.id(1L)
				.nickname("이진우")
				.build();
		//when
		when(memberService.read(anyLong())).thenReturn(memberReadResponseDto);

		//then
		mockMvc.perform(get("/users/1")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void createOAuthInfo() throws Exception {
		// given
		Member member = Member.builder()
				.username("my_username")
				.nickname("my_nickname")
				.password("my_password")
				.build();

		OAuth oAuth = OAuth.builder()
				.platform(OAuthPlatform.NAVER)
				.platformId("my_naver_unique_id")
				.member(member)
				.email("my_naver_email")
				.nickname("my_naver_nickname")
				.build();

		OAuthTokenDto oAuthTokenDto = OAuthTokenDto.builder()
				.token("my_oauth_token")
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(memberService.createOAuthInfo(any(Member.class), any(OAuthPlatform.class), any(OAuthTokenDto.class))).thenReturn(oAuth);

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
		Member member = Member.builder()
				.username("my_username")
				.nickname("my_nickname")
				.password("my_password")
				.build();

		OAuth oAuth = OAuth.builder()
				.platform(OAuthPlatform.NAVER)
				.platformId("my_naver_unique_id")
				.member(member)
				.email("my_naver_email")
				.nickname("my_naver_nickname")
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(memberService.deleteOAuthInfo(any(Member.class), any(OAuthPlatform.class))).thenReturn(oAuth);

		// then
		mockMvc.perform(delete("/users/oauth/naver")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}