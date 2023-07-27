package org.swmaestro.repl.gifthub.auth.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private MemberService memberService;

	@MockBean
	private JwtProvider jwtProvider;

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
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(1L));
	}
}