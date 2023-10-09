package org.swmaestro.repl.gifthub.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
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
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
	@Mock
	private MemberService memberService;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenService refreshTokenService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		memberService = new MemberServiceImpl(memberRepository, passwordEncoder, jwtProvider, refreshTokenService);
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

		// then
		Assertions.assertThatThrownBy(() -> memberService.create(signUpDto)).isInstanceOf(BusinessException.class);
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

		// then
		Assertions.assertThatThrownBy(() -> memberService.create(signUpDto)).isInstanceOf(BusinessException.class);
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
		Assertions.assertThatThrownBy(() -> memberService.create(signUpDto)).isInstanceOf(BusinessException.class);
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
		Member result = memberService.read(signUpDto.getUsername());

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

		// Then
		Assertions.assertThatThrownBy(() -> memberService.create(signUpDto1)).isInstanceOf(BusinessException.class);
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
