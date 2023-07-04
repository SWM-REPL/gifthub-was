package org.swmaestro.repl.gifthub.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class MemberServiceTest {
	@Autowired
	private MemberServiceImpl memberService;

	@MockBean
	private SpringDataJpaMemberRepository memberRepository;

	@BeforeEach
	void setUp() {
		memberService = new MemberServiceImpl(memberRepository);
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

		// then
		verify(memberRepository).save(member);
	}

	@Test
	void passwordEncryption() {
		// given
		String testPassword = "abc123";
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("jinlee1703")
			.password(testPassword)
			.nickname("이진우")
			.build();

		// when
		memberService.signUp(member);

		// then
		assertThat(memberRepository.findById(Long.valueOf(1)).get().getPassword()).isNotEqualTo(testPassword);
	}
}