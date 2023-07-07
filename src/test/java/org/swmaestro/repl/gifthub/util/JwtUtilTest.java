package org.swmaestro.repl.gifthub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
	@Mock
	private JwtUtil jwtUtil;

	@Test
	@DisplayName("generateToken test")
	void genderateToken() {
		// given
		String username = "jinwoolee";

		// when
		jwtUtil.generateToken(username);

		// then
		verify(jwtUtil, times(1)).generateToken(username);
	}

	@Test
	@DisplayName("validateToken test")
	void validateToken() {
		// given
		String username = "jinwoolee";

		// when
		when(jwtUtil.generateToken(anyString())).thenReturn("myawesomejwt");
		when(jwtUtil.validateToken(anyString())).thenReturn(true);

		String myJwtToken = jwtUtil.generateToken(username);

		// then
		assertTrue(jwtUtil.validateToken(myJwtToken));
		verify(jwtUtil, times(1)).validateToken(myJwtToken);
	}
}