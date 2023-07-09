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
import org.swmaestro.repl.gifthub.util.JwtProvider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

	@Mock
	private JwtProvider jwtProvider;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		memberService = new MemberServiceImpl(memberRepository, passwordEncoder, jwtProvider);
	}

	/*
	 * 회원가입 전체 로직 테스트
	 */
	@Test
	@DisplayName("signUp logic test")
	void create() {
		// given
		SignUpDto signUpDto = SignUpDto.builder()
			.username("jinlee1703")
			.password("abc123##")
			.nickname("이진우")
			.build();

		// when
		memberService.create(signUpDto);

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
		SignUpDto signUpDto = SignUpDto.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname("이진우")
			.build();

		// when
		Member member = Member.builder()
			.username(signUpDto.getUsername())
			.password("hashed_password")
			.nickname(signUpDto.getNickname())
			.build();

		memberService.create(signUpDto);
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
		SignUpDto signUpDto = SignUpDto.builder()
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
		memberService.create(signUpDto);

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
		SignUpDto signUpDto = SignUpDto.builder()
			.username(testUsername)
			.password(testPassword)
			.nickname(testNickname)
			.build();

		// when
		memberService.create(signUpDto);
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
		SignUpDto signUpDto = SignUpDto.builder()
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
		memberService.create(signUpDto);

		// then
		verify(memberRepository, times(0)).save(any(Member.class));
	}


	/*
	 * 회원 조회 테스트
	 */
	@Test
	@DisplayName("find member test")
	void read() {
		// given
		SignUpDto signUpDto = SignUpDto.builder()
			.username("jinlee1703")
			.password("abc123##")
			.nickname("이진우")
			.build();

		Member member = Member.builder()
			.username("jinlee1703")
			.password("abc123##")
			.nickname("이진우")
			.build();

		memberService.create(signUpDto);

		when(memberRepository.findByUsername(signUpDto.getUsername())).thenReturn(member);

		// when
		Member result = memberService.read(signUpDto);

		// then
		verify(memberRepository, times(2)).findByUsername(any(String.class));
		assertNotNull(result);
		assertEquals(signUpDto.getUsername(), result.getUsername());
	}

	/*
	 * 전체 회원의 수 조회 테스트
	 */
	@Test
	@DisplayName("count member test")
	void count() {
		// given
		SignUpDto signUpDto1 = SignUpDto.builder()
			.username("user1")
			.password("password1")
			.nickname("User 1")
			.build();
		memberService.create(signUpDto1);

		SignUpDto signUpDto2 = SignUpDto.builder()
			.username("user2")
			.password("password2")
			.nickname("User 2")
			.build();
		memberService.create(signUpDto2);

		SignUpDto signUpDto3 = SignUpDto.builder()
			.username("user3")
			.password("password3")
			.nickname("User 3")
			.build();
		memberService.create(signUpDto3);

		// Mock memberRepository.count() to return a fixed count value
		when(memberRepository.count()).thenReturn(3L);

		// when
		int count = memberService.count();

		// then
		assertEquals(3, count);
		verify(memberRepository, times(1)).count();
	}

	/*
	 * 전체 회원 정보 조회 테스트
	 */
	@Test
	@DisplayName("find all members test")
	void list() {
		// given
		Member member1 = Member.builder()
			.username("user1")
			.password("password1")
			.nickname("User 1")
			.build();

		Member member2 = Member.builder()
			.username("user2")
			.password("password2")
			.nickname("User 2")
			.build();

		List<Member> expectedMembers = new ArrayList<>();
		expectedMembers.add(member1);
		expectedMembers.add(member2);

		when(memberRepository.findAll()).thenReturn(expectedMembers);

		// when
		List<Member> actualMembers = memberService.list();

		// then
		assertEquals(expectedMembers, actualMembers);
		verify(memberRepository, times(1)).findAll();
	}
}
