package org.swmaestro.repl.gifthub.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
		jwtProvider.generateToken(username, 1L);

		// then
		verify(jwtProvider, times(1)).generateToken(username, 1L);
	}

	@Test
	@DisplayName("validateToken test")
	void validateToken() {
		// given
		String username = "jinwoolee";

		// when
		when(jwtProvider.generateToken(anyString(), 1L)).thenReturn("myawesomejwt");
		when(jwtProvider.validateToken(anyString())).thenReturn(true);

		String myJwtToken = jwtProvider.generateToken(username, 1L);

		// then
		assertTrue(jwtProvider.validateToken(myJwtToken));
		verify(jwtProvider, times(1)).validateToken(myJwtToken);
	}
}