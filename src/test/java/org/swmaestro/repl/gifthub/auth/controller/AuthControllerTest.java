package org.swmaestro.repl.gifthub.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.service.MemberService;

import static org.mockito.BDDMockito.given;
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

	@Test
	public void signUpTest() throws Exception {
		SignUpDto signUpDto = SignUpDto.builder()
			.username("jinlee1703")
			.password("abc123##")
			.nickname("이진우")
			.build();

		given(memberService.create(signUpDto)).willReturn("myawesomejwt");

		mockMvc.perform(post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signUpDto)))
			.andExpect(status().isOk());
	}
}
