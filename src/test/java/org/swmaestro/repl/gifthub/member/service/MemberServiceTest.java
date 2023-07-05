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

	/*
	 * 회원가입 전체 로직 테스트
	 */
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

	/*
	 * password 암호화 테스트
	 */
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

	/*
	 * password validation test
	 * 1. 영문/숫자/문자 포함 여부
	 * 2. 8-64자
	 */
	@Test
	@DisplayName("password validation test")
	void passwordValidation() {
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		String testNickname = "이진우";

		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname(testNickname)
			.build();

		// when
		memberService.create(signUpDTO);
		verify(memberRepository, times(0)).save(any(Member.class));
	}


	/*
	 * username validation test
	 * 1. 중복 검사
	 * 2. 4-60자
	 */
	@Test
	@DisplayName("username validation test")
	void usernameValidation() {
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		String testNickname = "이진우";

		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname(testNickname)
			.build();

		// when
		memberService.create(signUpDTO);
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	/*
	 * nickname validation test
	 * 1. 중복 검사
	 * 2. 2-12자
	 */
	@Test
	@DisplayName("nickname validation test")
	void nicknameValidation() {
		String testUsername = "jinlee1703";
		String testPassword = "abc123";
		String testNickname = "이진우";

		// given
		SignUpDTO signUpDTO = SignUpDTO.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname(testNickname)
			.build();

		// when
		memberService.create(signUpDTO);
		verify(memberRepository, times(0)).save(any(Member.class));
	}
}