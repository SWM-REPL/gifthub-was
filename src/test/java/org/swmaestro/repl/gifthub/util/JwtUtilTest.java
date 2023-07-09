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
	private JwtProvider jwtProvider;

	@Test
	@DisplayName("generateToken test")
	void genderateToken() {
		// given
		String username = "jinwoolee";

		// when
		jwtProvider.generateToken(username);

		// then
		verify(jwtProvider, times(1)).generateToken(username);
	}

	@Test
	@DisplayName("validateToken test")
	void validateToken() {
		// given
		String username = "jinwoolee";

		// when
		when(jwtProvider.generateToken(anyString())).thenReturn("myawesomejwt");
		when(jwtProvider.validateToken(anyString())).thenReturn(true);

		String myJwtToken = jwtProvider.generateToken(username);

		// then
		assertTrue(jwtProvider.validateToken(myJwtToken));
		verify(jwtProvider, times(1)).validateToken(myJwtToken);
	}
}