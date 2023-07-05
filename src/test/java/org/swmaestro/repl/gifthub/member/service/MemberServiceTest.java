package org.swmaestro.repl.gifthub.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
	@Mock
	private MemberServiceImpl memberService;

	@Mock
	private SpringDataJpaMemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		memberService = new MemberServiceImpl(memberRepository, passwordEncoder);
	}

	@Test
	@DisplayName("signUp logic test")
	void create() {
		// given
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("jinlee1703")
			.password("abc123")
			.nickname("이진우")
			.build();

		// when
		memberService.create(member);

		// then
		verify(memberRepository).save(member);
	}

	@Test
	@DisplayName("password encryption logic test")
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
		when(memberRepository.save(any(Member.class))).thenReturn(member);
		memberService.create(member);

		// then
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("Validation Check - Username Test")
	void validationCheck1() {
		// given
		String testPassword = "abc123";
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.password(testPassword)
			.nickname("이진우")
			.build();

		// when
		when(memberRepository.save(any(Member.class))).thenReturn(member);
		memberService.create(member);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	@Test
	@DisplayName("Validation Check - password Test")
	void validationCheck2() {
		// given
		String testPassword = "abc123";
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("jinlee1703")
			.nickname("이진우")
			.build();

		// when
		when(memberRepository.save(any(Member.class))).thenReturn(member);
		memberService.create(member);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	@Test
	@DisplayName("Validation Check - nickname Test")
	void validationCheck3() {
		// given
		String testPassword = "abc123";
		Member member = Member.builder()
			.id(Long.valueOf(1))
			.username("jinlee1703")
			.password(testPassword)
			.build();

		// when
		when(memberRepository.save(any(Member.class))).thenReturn(member);
		memberService.create(member);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}
}