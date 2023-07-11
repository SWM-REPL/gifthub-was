package org.swmaestro.repl.gifthub.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AuthServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthServiceImpl authService;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private RefreshTokenService refreshTokenService;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		authService = new AuthServiceImpl(memberRepository, passwordEncoder, jwtProvider, refreshTokenService);
	}

	/*
	 * 비밀번호 검증 로직 성공 테스트
	 */
	@Test
	void verifyPasswordSuccess() {
		//given
		String username = "jinlee1703";
		String password = "abc123##";
		String encodedPassword = "abc123##XX";

		SignInDto loginDto = SignInDto.builder()
				.username(username)
				.password(password)
				.build();

		Member member = Member.builder()
				.username(username)
				.password(password)
				.nickname("이진우")
				.build();


		when(memberRepository.findByUsername(loginDto.getUsername())).thenReturn(member);
		when(passwordEncoder.matches(loginDto.getPassword(), member.getPassword())).thenReturn(true);
		when(jwtProvider.generateToken(member.getUsername())).thenReturn("accessToken");
		when(jwtProvider.generateRefreshToken(member.getUsername())).thenReturn("refreshToken");

		// When
		TokenDto tokenDto = authService.signIn(loginDto);

		// Assert
		assertNotNull(tokenDto);
		assertEquals("accessToken", tokenDto.getAccessToken());
		assertEquals("refreshToken", tokenDto.getRefreshToken());
		verify(refreshTokenService, times(1)).storeRefreshToken(any(TokenDto.class), eq(username));
	}

	/*
	 * 비밀번호 검증 로직 실패 테스트
	 */
	@Test
	void verifyPasswordFail() {
		//given
		SignInDto loginDto = SignInDto.builder()
				.username("jinlee1703")
				.password("abc123##")
				.build();
		Member member = Member.builder()
				.username("jinlee1703")
				.password("abc123##XX")
				.nickname("이진우")
				.build();

		// Mocking behavior of the repository
		when(memberRepository.findByUsername(loginDto.getUsername())).thenReturn(null);

		// When
		TokenDto result = authService.signIn(loginDto);

		// Then
		assertEquals(null, result);
	}
}
