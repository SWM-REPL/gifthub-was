package org.swmaestro.repl.gifthub.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swmaestro.repl.gifthub.member.dto.SignUpDTO;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
		String testUsername = "jinlee1703";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password("abc123")
			.nickname("이진우")
			.build();

		// when
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(1)).save(any(Member.class));
	}

	@Test
	@DisplayName("password encryption logic test")
	void passwordEncryption() {
		// given
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname("이진우")
			.build();

		// when
		Member member = Member.builder()
			.username(signUpDTO.getUsername())
			.password("hashed_password")
			.nickname(signUpDTO.getNickname())
			.build();

		memberService.create(signUpDTO);
		when(memberRepository.findByUsername(any(String.class))).thenReturn(member);

		// then
		assertNotEquals(memberRepository.findByUsername(testUsername).getPassword(), testPassword);
	}

	@Test
	@DisplayName("Validation Check - Username Test")
	void validationCheck1() {
		// given
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.password(testPassword)
			.nickname("이진우")
			.build();

		// when
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	@Test
	@DisplayName("Validation Check - password Test")
	void validationCheck2() {
		// given
		String testUsername = "jinlee1703";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.nickname("이진우")
			.build();

		// when
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	@Test
	@DisplayName("Validation Check - nickname Test")
	void validationCheck3() {
		// given
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password(testPassword)
			.build();

		// when
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}
}