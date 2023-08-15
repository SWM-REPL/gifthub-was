package org.swmaestro.repl.gifthub.auth.service;

class AuthServiceTest {
	// @Mock
	// private MemberRepository memberRepository;
	//
	// @Mock
	// private PasswordEncoder passwordEncoder;
	//
	// @Mock
	// private AuthServiceImpl authService;
	//
	// @Mock
	// private JwtProvider jwtProvider;
	//
	// @Mock
	// private RefreshTokenService refreshTokenService;
	//
	// @BeforeEach
	// public void setUp() {
	// 	MockitoAnnotations.openMocks(this);
	// 	authService = new AuthServiceImpl(memberRepository, passwordEncoder, jwtProvider, refreshTokenService);
	// }
	//
	// /*
	//  * 비밀번호 검증 로직 성공 테스트
	//  */
	// @Test
	// void loginSuccess() {
	// 	//given
	// 	String username = "jinlee1703";
	// 	String password = "abc123##";
	// 	String encodedPassword = "abc123##XX";
	//
	// 	SignInDto loginDto = SignInDto.builder()
	// 			.username(username)
	// 			.password(password)
	// 			.build();
	//
	// 	Member member = Member.builder()
	// 			.username(username)
	// 			.password(password)
	// 			.nickname("이진우")
	// 			.build();
	//
	// 	when(memberRepository.findByUsername(loginDto.getUsername())).thenReturn(member);
	// 	when(passwordEncoder.matches(loginDto.getPassword(), member.getPassword())).thenReturn(true);
	// 	when(jwtProvider.generateToken(member.getUsername())).thenReturn("accessToken");
	// 	when(jwtProvider.generateRefreshToken(member.getUsername())).thenReturn("refreshToken");
	//
	// 	// When
	// 	TokenDto tokenDto = authService.signIn(loginDto);
	//
	// 	// Assert
	// 	assertNotNull(tokenDto);
	// 	assertEquals("accessToken", tokenDto.getAccessToken());
	// 	assertEquals("refreshToken", tokenDto.getRefreshToken());
	// 	verify(refreshTokenService, times(1)).storeRefreshToken(any(TokenDto.class), eq(username));
	// }
	//
	// /*
	//  * 비밀번호 검증 로직 실패 테스트(가입한 회원이 아닌 경우)
	//  */
	// @Test
	// void loginFailByUsername() {
	// 	//given
	// 	SignInDto loginDto = SignInDto.builder()
	// 			.username("jinlee1703")
	// 			.password("abc123##")
	// 			.build();
	// 	Member member = Member.builder()
	// 			.username("jinlee1703")
	// 			.password("abc123##XX")
	// 			.nickname("이진우")
	// 			.build();
	//
	// 	// When
	// 	when(memberRepository.findByUsername(loginDto.getUsername())).thenReturn(null);
	//
	// 	// Then
	// 	Assertions.assertThatThrownBy(() -> authService.signIn(loginDto)).isInstanceOf(BusinessException.class);
	// }
	//
	// /*
	//  * 비밀번호 검증 로직 실패 테스트(비밀번호가 일치하지 않는 경우)
	//  */
	// @Test
	// void loginFailByPassword() {
	// 	//given
	// 	SignInDto loginDto = SignInDto.builder()
	// 			.username("jinlee1703")
	// 			.password("abc123##")
	// 			.build();
	// 	Member member = Member.builder()
	// 			.username("jinlee1703")
	// 			.password("abc123##XX")
	// 			.nickname("이진우")
	// 			.build();
	//
	// 	// When
	// 	when(memberRepository.findByUsername(loginDto.getUsername())).thenReturn(member);
	// 	when(passwordEncoder.matches(loginDto.getPassword(), member.getPassword())).thenReturn(false);
	//
	// 	// Then
	// 	Assertions.assertThatThrownBy(() -> authService.signIn(loginDto)).isInstanceOf(BusinessException.class);
	// }
}
