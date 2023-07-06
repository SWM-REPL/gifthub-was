package org.swmaestro.repl.gifthub.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.SpringDataJpaMemberRepository;

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
		SignUpDto signUpDTO = SignUpDto.builder()
			.username("jinlee1703")
			.password("abc123##")
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
		SignUpDto signUpDTO = SignUpDto.builder()
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
	 * 중복된 username 테스트
	 */
	@Test
	@DisplayName("duplicated username test")
	void duplicatedUsername() {
		String testUsername = "jinlee1703";

		// given
		SignUpDto signUpDTO = SignUpDto.builder()
			.username(testUsername)
			.password("abc123##")
			.nickname("이진우")
			.build();

		// when
		Member member = Member.builder()
			.username(testUsername)
			.password("hashed_password")
			.nickname("이진수")
			.build();

		when(memberRepository.findByUsername(any(String.class))).thenReturn(member);
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	/*
	 * password 영문/숫자/문자 포함 여부 테스트
	 */
	@Test
	@DisplayName("password validation test")
	void passwordValidation() {
		String testUsername = "jinlee1703";
		String testPassword = "abc123123";
		String testNickname = "이진우";

		// given
		SignUpDto signUpDTO = SignUpDto.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname(testNickname)
			.build();

		// when
		memberService.create(signUpDTO);
		verify(memberRepository, times(0)).save(any(Member.class));
	}

	/*
	 * nickname 중복 검사 테스트
	 */
	@Test
	@DisplayName("duplicated nickname test")
	void nicknameValidation() {
		String testNickname = "이진우";

		// given
		SignUpDto signUpDTO = SignUpDto.builder()
			.username("jinlee1703")
			.password("abc123##")
			.nickname(testNickname)
			.build();

		// when
		Member member = Member.builder()
			.username("testUsername")
			.password("hashed_password")
			.nickname(testNickname)
			.build();

		when(memberRepository.findByNickname(any(String.class))).thenReturn(member);
		memberService.create(signUpDTO);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}
}