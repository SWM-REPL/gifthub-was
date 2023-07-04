package org.swmaestro.repl.gifthub.member.service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class MemberServiceTest {
	@Autowired
	MemberService memberService;

	@Autowired
	SpringDataJpaMemberRepository memberRepository;


	// Test 메서드를 매번 실행하기 전에 MemberService에 가짜 객체를 주입
	@BeforeEach
	void setUp() {
		memberService = new MemberService(memberRepository);
	}

	@Test
	void signUp() {
		// given
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("jinlee1703")
			.password("abc123")
			.nickname("이진우")
			.build();

		// when
		memberService.signUp(member);

		System.out.println(memberRepository.findAll().get(0).getUsername());

		// then
		assertTrue(memberRepository.findById(Long.valueOf(1)).isPresent());
	}
}