package org.swmaestro.repl.gifthub.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MemberServiceTest {
	private MockMvc mockMvc;

	private MemberService memberService;

	@MockBean
	private SpringDataJpaMemberRepository memberRepository;


	// Test 메서드를 매번 실행하기 전에 MemberService에 가짜 객체를 주입
	@BeforeEach
	void setUp() {
		// given
		memberService = new MemberService(memberRepository);
	}

	@Test
	void signUp() {
		// given
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("test")
			.password("test")
			.nickname("test")
			.build();

		// when
		memberService.signUp(member);

		// then
		Member c = memberRepository.findById(Long.valueOf(1)).get();
		assertEquals(member, Long.valueOf(1));
	}
}